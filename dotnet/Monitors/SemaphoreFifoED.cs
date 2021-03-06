using SynchUtils;
using System;
using System.Collections.Generic;
using System.Threading;

namespace Monitors
{
    public class SemaphoreFifoED
    {
        /**
         *  this class instances represent
         *  a pending acquire request
         */
        private class Request
        {
            public readonly int units;
            public bool done;

            public Request(int units) {
                this.units = units;
                this.done = false;
            }
        }

        private int units;
        private readonly LinkedList<Request> requests;
        private readonly object monitor;


        public SemaphoreFifoED(int initial) {
            if (initial > 0)
                units = initial;
            monitor = new object();

            requests = new LinkedList<Request>();
        }

        /**
         * An auxiliary method that tries
         * to process all possible pending requests
         */
        private void NotifyWaiters() {
            bool toNotify = false;
            while (requests.Count > 0 && units >= requests.First.Value.units) {

                Request r = requests.First.Value;
                requests.RemoveFirst();
                units -= r.units;
                r.done = true;
                toNotify = true;
            }
            if (toNotify) {
                Monitor.PulseAll(monitor);
            }
        }

        public bool Acquire(int n, int millis) {
            lock (monitor) {

                // fast path
                if (units >= n && requests.Count == 0) {
                    units -= n;
                    return true;
                }
                if (millis == 0) return false;

                // prepare wait
                var req = new Request(n);
                LinkedListNode<Request> node = requests.AddLast(req);
                TimeoutHolder th = new TimeoutHolder(millis);

                // do wait
                do {
                    try {
                        // non interruption path
                        Monitor.Wait(monitor, th.Remaining);

                        if (req.done) return true;
                        if (th.Timeout) {
                            requests.Remove(node);
                            // note that we must notify
                            // in this case, since the
                            // timeouted request
                            // can be at the front of the list
                            NotifyWaiters();
                            return false;
                        }
                    }
                    catch (ThreadInterruptedException e) {
                        // note that the request
                        // can be already accepted
                        // so in the case we return success
                        // but redoing a thread interrupt request
                        if (req.done) {
                            Thread.CurrentThread.Interrupt();
                            return true;
                        }
                        // note that we must notify
                        // in this case, since the
                        // interrupted request
                        // can be at the front of the list
                        requests.Remove(node);
                        NotifyWaiters();
                        throw e;
                    }
                }
                while (true);
            }
        }


        public void Release(int n) {
            lock (monitor) {
                units += n;
                NotifyWaiters();
            }
        }
    }

}
