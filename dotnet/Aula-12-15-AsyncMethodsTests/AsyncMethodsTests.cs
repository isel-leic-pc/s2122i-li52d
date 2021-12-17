using NUnit.Framework;
using Aula_12_15_AsyncMethods;
using System.Linq;
using System;
using System.Threading.Tasks;
using AsyncIntro2;

namespace Aula_12_15_AsyncMethodsTests
{
    public class Tests
    {
        [SetUp]
        public void Setup() {
        }


        [Test]
        public void EvenIteratorTest() {

            foreach (int e in Iterators
                              .Evens()
                              .TakeWhile(n => n < 20)) {
                Console.WriteLine(e);
            }
            Assert.Pass();
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