using NUnit.Framework;
using System;
using System.Threading;
using System.Threading.Tasks;
using AsyncIntro2;

namespace Aula_12_10_AsyncIntroTests
{
    using static Aula_12_03.TaskOpers;

    public class Tests
    {
        [SetUp]
        public void Setup() {
        }

        [Test]
        public  void DownloadToFileAsyncTest() {
            Task<bool> transfer = null;
            try {
                
                transfer = DownloadToFileAsync("http://www.sic.pt/favicon.ico", "sic_favicon.ico" );
                Console.WriteLine("Response status = ");
                //transfer.Wait();

                Console.WriteLine(transfer.Result);
            }
            catch (AggregateException e) {
                Console.WriteLine("Task status = {0}", transfer.Status);
                foreach (Exception ec in e.Flatten().InnerExceptions) {
                    Console.WriteLine("type is {0}, msg = {1}",
                                      ec.GetType(),
                                      ec.Message);
                }
            }
        }

        [Test]
        public void CancellationTaskTest() {
            CancellationTokenSource cts = new CancellationTokenSource();

            Task<int> task = TaskExamples.ManualTaskCreationWithCancellation(cts.Token);

            Thread.Sleep(4000);
            cts.Cancel();

            try {
                int val = task.Result;
                Console.WriteLine("result  is {0}", task.Result);
            }
            catch(AggregateException e) {
                Console.WriteLine("Task status = {0}", task.Status);
                foreach (Exception ec in e.InnerExceptions) {
                    Console.WriteLine("type is {0}, msg = {1}",
                                      ec.GetType(),
                                      ec.Message);
                }
            }
            catch(Exception e) {
                Console.WriteLine("exception type is {0}", e.GetType());
            }
        }

        [Test]
        public void MultipleDownloadToFileAsyncTest() {
            string[] urls = {
                    "http://www.sic.pt/favicon.ico",
                    "http://www.rtp.pt/favicon.ico",
                    "http://www.tvi.pt/favicon.ico"
            };

            string[] names = {
                "sic_favicon.ico",
                "rtp_favicon.ico",
                "tvi_favicon.ico"
            };



            Task allDone = TaskUtils.MultipleDownloadToFilesAsync(urls, names);

            allDone.Wait();

            Console.WriteLine("All files created!");   
        }
    }
}