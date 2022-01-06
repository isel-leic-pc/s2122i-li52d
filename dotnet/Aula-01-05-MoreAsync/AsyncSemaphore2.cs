using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Aula_01_05_MoreAsync
{
    public class AsyncSemaphore2
    {
        private int units;                                      // current units
        private readonly object monitor;                        // used just as a lock, to define critical sections
        private readonly LinkedList<PendingRequest> requests;   // list for mantain pending acquire requests


        private static readonly Task<bool> TrueTask = Task.FromResult(true);
        private static readonly Task<bool> FalseTask = Task.FromResult(false);

        private Action<object> cancelAction;                    // action called on cancellation
        private TimerCallback timeoutAction;                    // action called on timeout

        private class PendingRequest : TaskCompletionSource<bool>
        {
            internal readonly int n;
            private Timer timer;
            private CancellationTokenRegistration cancelRegist;
            private CancellationToken token;


            // this flag is crucial to avoid multiple concurrent completions
            // (success, cancalation,timeout)
            internal bool Completed
            {
                get;
                set;
            }


            internal PendingRequest(int n, CancellationToken token) {
                this.n = n;
                this.token = token;
            }


            internal void RegistCancellation(Action<Object> cancelAction, object node) {
                if (token.CanBeCanceled)
                    cancelRegist = token.Register(cancelAction, node);
            }

            internal void RegistTimeout(int timeout, TimerCallback cb, object node) {
                if (timeout != Timeout.Infinite)
                    timer = new Timer(cb, node, timeout, Timeout.Infinite);
            }


            // auxilairy completion and disposing methods
            // to avoid deadlocks and reentrancy problems the implementaion ensure
            // that this methods are called outside of the semaphore lock!
            internal void Dispose() {
                timer?.Dispose();
                cancelRegist.Dispose();
            }

            // To complete with timeout
            internal void SetTimeout() {
                SetResult(false);

                // Just dispose the CancellationTokenRegistration since this is doing by the timer callback
                // and the timer resources will be released after the callback completion
                cancelRegist.Dispose();
            }

            // To complete with success
            internal void SetSuccess() {
                SetResult(true);
                Dispose();
            }

            // To complete cancelled
            internal new void SetCanceled() {
                base.SetCanceled();

                // Just dispose the timer since this is doing by the cancellation callback
                // and the CancellationTokenRegistration resources will be released after callback completion
                timer?.Dispose();
            }
        }


        public AsyncSemaphore2(int initialUnits) {
            if (initialUnits > 0) {
                units = initialUnits;

            }
            monitor = new object();
            requests = new LinkedList<PendingRequest>();
            cancelAction = o => TryProcessCancellation((LinkedListNode<PendingRequest>)o);
            timeoutAction = o => TryProcessTimeout((LinkedListNode<PendingRequest>)o);
        }


        public Task<bool> AcquireAsync(int n) {
            return AcquireAsync(n, CancellationToken.None, Timeout.Infinite);
        }

        public Task<bool> AcquireAsync(int n, CancellationToken token, int millis ) {
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
               
                var req = new PendingRequest(n, token);
                LinkedListNode< PendingRequest> node = requests.AddLast(req);

                req.RegistCancellation(cancelAction, node);
                req.RegistTimeout(millis, timeoutAction, node);

                return req.Task;
            }

        }

        public void Release(int n) {
            List<PendingRequest> satisfiedRequests;
            lock (monitor) {
                units += n;
                satisfiedRequests = SatisfyPendingRequests();
            }

            // we must complete the satisfied requests out of the monitor lock
            // to avoid a possible registered synchronous continuation
            // to access the semaphore while we own the lock
            CompletePendingAcquires(satisfiedRequests);
        }

        // Auxiliary method used on cancellation and timeout handlers
        private bool RemoveIfNotCompleted(LinkedListNode<PendingRequest> node) {
            bool removed = false;
            List<PendingRequest> satisfiedAcquires = new List<PendingRequest>();
            lock (monitor) {
                PendingRequest req = node.Value;
                if (!req.Completed) {
                    requests.Remove(node);
                    req.Completed = true;
                    removed = true;
                    satisfiedAcquires = SatisfyPendingRequests();
                }
            }
            if (removed) {
                CompletePendingAcquires(satisfiedAcquires);
            }
            return removed;
        }

        // To process a cancellation request
        private void TryProcessCancellation(LinkedListNode<PendingRequest> node) {
            if (RemoveIfNotCompleted(node))
                node.Value.SetCanceled();
        }

        // To process a timeout request
        private void TryProcessTimeout(LinkedListNode<PendingRequest> node) {
            if (RemoveIfNotCompleted(node))
                node.Value.SetTimeout();
        }

        // Auxiliary method to build the successfully then pending acquire  list.
        // This method SHALL be called INSIDE the monitor lock
        private List<PendingRequest> SatisfyPendingRequests() {
            List<PendingRequest> satisfiedAcquires = new List<PendingRequest>();

            while (requests.Count > 0 && units >= requests.First.Value.n) {
                PendingRequest req = requests.First.Value;
                requests.RemoveFirst();
                satisfiedAcquires.Add(req);
                units -= req.n;
                req.Completed = true;
            }
            return satisfiedAcquires;
        }

        // Auxiliary method to complete successfully then pending acquire  list received
        // This method SHALL be called OUTSIDE the monitor lock
        private void CompletePendingAcquires(List<PendingRequest> satisfiedAcquires) {
            foreach (PendingRequest req in satisfiedAcquires) {
                req.SetSuccess();
            }
        }
       
    }
}
