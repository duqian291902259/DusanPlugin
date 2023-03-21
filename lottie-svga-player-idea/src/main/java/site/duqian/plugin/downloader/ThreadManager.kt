package site.duqian.plugin.downloader

import java.util.HashMap
import java.util.concurrent.*
import kotlin.math.max

/**
 * ThreadManager:简易的线程池管理类
 *
 * @author Dusan-杜乾 Created on 2017/6/13 - 13:46.
 * E-mail:duqian2010@gmail.com
 */
object ThreadManager {
    private const val TAG = "dq-ThreadManager"
    private var mBackgroundPool: ThreadPoolProxy? = null
    private val mBackgroundLock = Any()
    private var mDownloadPool: ThreadPoolProxy? = null
    private val mDownloadLock = Any()
    private val mMap: MutableMap<String, ThreadPoolProxy> = HashMap()
    private val mSingleLock = Any()
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()

    /**
     * 获取后台线程池,核心线程会一直存活。
     * CPU密集型任务配置尽可能少的线程数量：CPU核数+1个线程的线程池
     */
    val backgroundPool: ThreadPoolProxy
        get() {
            synchronized(mBackgroundLock) {
                if (mBackgroundPool == null) {
                    val corePoolSize = max(CPU_COUNT, 2)
                    //d("$TAG dq-corePoolSize1=$corePoolSize")
                    mBackgroundPool = ThreadPoolProxy(corePoolSize + 1, Int.MAX_VALUE, 60L, false)
                }
                return mBackgroundPool!!
            }
        }

    /**
     * 获取一个用于文件并发下载的线程池
     * 修改核心线程数和最大线程数:
     * IO密集型任务线程并不是一直在执行任务，则应配置尽可能多的线程，CPU核数*2
     */
    val downloadPool: ThreadPoolProxy
        get() {
            synchronized(mDownloadLock) {
                if (mDownloadPool == null) {
                    val cpu = max(CPU_COUNT, 1)
                    val corePoolSize = cpu * 2
                    println("$TAG dq-corePoolSize2 cpu=$cpu，corePoolSize=$corePoolSize")
                    mDownloadPool = ThreadPoolProxy(corePoolSize, corePoolSize * 2, 60L, true)
                }
                return mDownloadPool!!
            }
        }

    /**
     * 获取一个单线程池，所有任务将会被按照加入的顺序执行，免除了同步开销的问题
     */
    fun getSinglePool(name: String): ThreadPoolProxy {
        synchronized(mSingleLock) {
            var singlePool = mMap[name]
            if (singlePool == null) {
                singlePool = ThreadPoolProxy(0, 1, 60L, false)
                mMap[name] = singlePool
            }
            return singlePool
        }
    }

    class ThreadPoolProxy
    /**
     * @param corePoolSize    核心线程数量
     * @param maximumPoolSize 最大线程数量
     * @param keepAliveTime   空闲线程存活时间，秒
     */ constructor(
        private val mCorePoolSize: Int,
        private val mMaximumPoolSize: Int,
        private val mKeepAliveTime: Long,
        private val mIsPriority: Boolean
    ) {
        lateinit var mPool: ThreadPoolExecutor

        init {
            initTreadPoolExecutor()
        }

        /**
         * 执行任务，当线程池处于关闭，将会重新创建新的线程池
         */
        @Synchronized
        fun execute(run: Runnable?) {
            if (run == null) {
                return
            }
            if (mPool.isShutdown) {
                //ThreadFactory是每次创建新的线程工厂
                initTreadPoolExecutor()
            }
            mPool.execute(run)
        }

        private fun initTreadPoolExecutor() {
            mPool = if (mIsPriority) { //使用优先级队列
                ThreadPoolExecutor(
                    mCorePoolSize,
                    mMaximumPoolSize,
                    mKeepAliveTime,
                    TimeUnit.SECONDS,
                    PriorityBlockingQueue(),
                    Executors.defaultThreadFactory(),
                    ThreadPoolExecutor.AbortPolicy()
                )
            } else { //队列任务
                ThreadPoolExecutor(
                    mCorePoolSize,
                    mMaximumPoolSize,
                    mKeepAliveTime,
                    TimeUnit.SECONDS,
                    LinkedBlockingQueue(),
                    Executors.defaultThreadFactory(),
                    ThreadPoolExecutor.AbortPolicy()
                )
            }
        }

        /**
         * 取消线程池中某个还未执行的任务
         */
        @Synchronized
        fun remove(run: Runnable?) {
            if (!mPool.isShutdown || mPool.isTerminating) {
                mPool.queue?.remove(run)
            }
        }

        /**
         * 是否包含某个任务
         */
        @Synchronized
        operator fun contains(run: Runnable?): Boolean {
            return if (!mPool.isShutdown || mPool.isTerminating) {
                mPool.queue?.contains(run) ?: false
            } else {
                false
            }
        }

        /**
         * 关闭线程池，
         *
         * @param isNow if true 立即终止线程池，并尝试打断正在执行的任务，清空任务缓存队列，返回尚未执行的任务。
         * if false ,确保所有已经加入的任务都将会被执行完毕才关闭,后面不接受任务
         */
        @Synchronized
        fun shutdown(isNow: Boolean) {
            if (!mPool.isShutdown || mPool.isTerminating) {
                if (isNow) {
                    mPool.shutdownNow()
                } else {
                    mPool.shutdown()
                }
            }
        }
    }
}