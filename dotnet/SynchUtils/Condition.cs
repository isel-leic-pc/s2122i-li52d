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
            Monitor.Exit(monitor); // leave monitor

            try {
                Monitor.Enter(condition);
                Monitor.Wait(condition, timeout);
            }
            finally {
                bool interrupted;
                EnterUninterruptibly(monitor, out interrupted);
                Monitor.Exit(condition);
                if (interrupted)
                    throw new ThreadInterruptedException();-
            }
        }

        /// <summary>
        /// usage:
        /// condition.Notify(monitor);
        /// </summary>
        /// <param name="condition"></param>
        /// <param name="monitor"></param>
        public static void Notify(this Object condition, Object monitor) {
           
           

        }

        public static void NotifyAll(this Object condition, Object monitor) {
          
        }
    }
}
