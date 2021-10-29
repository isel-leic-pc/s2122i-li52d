using System;
using System.Threading;

namespace SynchUtils
{
    
    public static class Condition
    {

        public static void EnterUninterruptibly(object monitor,
                                        out bool interrupted) {
            interrupted = false;
            do {
                try {
                    Monitor.Enter(monitor);
                    return;
                }
                catch(ThreadInterruptedException) {
                    interrupted = true;
                }
            }
            while (true);
        }

        /// <summary>
        ///  usage
        ///  condition.Wait(monitor, timeout)
        /// </summary>
        /// <param name="condition"></param>
        /// <param name="monitor"></param>
        /// <param name="timeout"></param>
        public static void Wait(this Object condition, 
            Object monitor, int timeout) {

            if (condition == monitor) {
                Monitor.Wait(monitor, timeout);
                return;
            }

            //This code in in a intermediate state
            // with many problems that can lead to 
            // inconsistencies and deadlock!
            // to solve in the next lecture!


           
            Monitor.Enter(condition);
            Monitor.Exit(monitor); // leave monitor



            try {
                Monitor.Wait(condition, timeout);
            }
            finally {
                bool interrupted;
                Monitor.Exit(condition);
                EnterUninterruptibly(monitor, out interrupted);
              
                if (interrupted)
                    throw new ThreadInterruptedException();
            }
        }

        /// <summary>
        /// usage:
        /// condition.Notify(monitor);
        /// </summary>
        /// <param name="condition"></param>
        /// <param name="monitor"></param>
        public static void Notify(this Object condition, Object monitor) {
            if (condition == monitor) {
                Monitor.Pulse(monitor);
                return;
            }

          
            bool interrupted;
            EnterUninterruptibly(condition, out interrupted);
            Monitor.Pulse(condition);
            Monitor.Exit(condition);
            if (interrupted)
                Thread.CurrentThread.Interrupt();

        }

        public static void NotifyAll(this Object condition, Object monitor) {
          
        }
    }
}
