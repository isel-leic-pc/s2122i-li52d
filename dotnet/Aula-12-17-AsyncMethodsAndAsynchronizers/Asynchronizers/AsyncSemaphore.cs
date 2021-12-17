using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Aula_12_16_AsyncMethodsAndAsynchronizers.Asynchronizers
{
    class AsyncSemaphore
    {
        private int units;
        private readonly object monitor;
        private readonly LinkedList<PendingRequest> requests;

        private static readonly Task<bool> TrueTask = Task.FromResult(true);
        private static readonly Task<bool> FalseTask = Task.FromResult(false);

        private  class PendingRequest : TaskCompletionSource<bool> 
        {
            internal readonly int n;
           
            internal PendingRequest(int n) {
                this.n = n;
            }
        }

        public AsyncSemaphore(int initialUnits) {
            if (initialUnits > 0) {
                units = initialUnits;
               
            }
            monitor = new object();
            requests = new LinkedList<PendingRequest>();
        }

        public Task<bool> AcquireAsync(int n) {
            return AcquireAsync(n, Timeout.Infinite, CancellationToken.None);
        }

        public  Task<bool> AcquireAsync(int n, int millis, CancellationToken token) {
            lock (monitor) {
                // fast path
                if (units >= n) {
                    units -= n;
                    return TrueTask;
                }
                if (millis == 0) {
                    return FalseTask;
                }
                token.ThrowIfCancellationRequested();
                // This is incomplete!
                // We need to refactor PendingRequest to create a timer
                // and a CancelRegistrationToken in order to 
                // support timeout and cancellation for pending requests
                // We will do this on next lecture
                var req = new PendingRequest(n);
                requests.AddLast(req);
                return req.Task;
            }
          
        }

        public void Release(int n) {
            LinkedList<PendingRequest> satisfiedRequests =
                new LinkedList<PendingRequest>();
            lock(monitor) {
                units += n;

                while(requests.Count > 0 &&
                      units >= requests.First.Value.n) {
                    PendingRequest req = requests.First.Value;
                    requests.RemoveFirst();
                    units -= req.n;
                    satisfiedRequests.AddFirst(req);
                }
            }

            // we must complete the satisfied requests out of the monitor lock
            // to avoid a possible registered synchronous continuation
            // to access the semaphore while we own the lock
            foreach(PendingRequest req in satisfiedRequests)
                req.SetResult(true);
        }
    }
}
