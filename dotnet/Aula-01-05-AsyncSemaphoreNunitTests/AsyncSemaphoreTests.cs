using NUnit.Framework;
using Aula_01_05_MoreAsync;
using System.Threading;
using System;
using System.Threading.Tasks;
using System.Diagnostics;

namespace Aula_01_05_AsyncSemaphoreNunitTests
{
    public class Tests
    {
        [SetUp]
        public void Setup() {
        }

        [Test]
        public void SemaphoreAsyncSimpleAcquire() {
            AsyncSemaphore2 asem = new AsyncSemaphore2(0);

            Thread t = new Thread(() =>
            {
                Debug.WriteLine("Releaser Thread is {0}", Thread.CurrentThread.ManagedThreadId);
                Thread.Sleep(1000);
                asem.Release(1);
            });
            t.Start();

            Debug.WriteLine("Test Thread is {0}", Thread.CurrentThread.ManagedThreadId);

            Task<bool> task =
                asem.AcquireAsync(1)
                    .ContinueWith(ant =>
                    {
                        Debug.WriteLine("Continuation Thread is {0}", Thread.CurrentThread.ManagedThreadId);
                        return ant.Result;
                    } /*, TaskContinuationOptions.ExecuteSynchronously*/);

            try {
                bool res = task.Result;
                Assert.AreEqual(true, res);
            }
            catch (Exception e) {
                Assert.Fail();
            }

        }

        [Test]
        public void SemaphoreAsyncSimpleTimeoutTest() {
            AsyncSemaphore2 asem = new AsyncSemaphore2(0);

            Task<bool> task = asem.AcquireAsync(1, CancellationToken.None, 2000);
            Assert.IsFalse(task.IsCompleted);
            Thread.Sleep(3000);
            try {
                bool res = task.Result;
            }
            catch (AggregateException e) {
                Assert.IsTrue(task.IsFaulted && e.InnerException is TimeoutException);
            }

        }

        [Test]
        public void SemaphoreAsyncCancellation() {
            // Auto cancellation after 2 seconds
            CancellationTokenSource cts = new CancellationTokenSource(2000);
            AsyncSemaphore2 asem = new AsyncSemaphore2(0);



            Debug.WriteLine("Test Thread is {0}", Thread.CurrentThread.ManagedThreadId);

            Task<bool> task =
                asem.AcquireAsync(1, cts.Token, Timeout.Infinite)
                    .ContinueWith(ant =>
                    {
                        Debug.WriteLine("Continuation Thread is {0}", Thread.CurrentThread.ManagedThreadId);
                        return ant.Result;
                    } /*, TaskContinuationOptions.ExecuteSynchronously*/);

            try {
                bool res = task.Result;
                Assert.Fail();

            }
            catch (AggregateException e) {
                e = e.Flatten();
                Assert.IsTrue(task.IsFaulted && e.InnerException is TaskCanceledException);
            }

        }
    }
}
