
using System;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;

 
namespace ShowFavIconAsync
{

    static class AsyncModel
    {

        /// <summary>
        /// A solution for async image download operation.
        /// Try to describe why is the Unwrap necessary!
        /// 
        /// More alternatives exists on Model1 class
        /// </summary>
        /// <param name="url"></param>
        /// <returns></returns>
        public static Task<Image> DownloadImageFromUrlAsync(string url, int spendTime) {
            HttpClient client = new HttpClient();
            client.DefaultRequestHeaders.Add("User-Agent", "Mozilla/5.0");

            return Task.Delay(spendTime)
                .ContinueWith(__ => client.GetStreamAsync(url))
                .Unwrap()
                .ContinueWith(ant => {
                    MemoryStream ms = new MemoryStream();
                    return ant.Result.CopyToAsync(ms).ContinueWith(_ => ms);
                })
                .Unwrap()
                .ContinueWith(ant2 => {
                    client.Dispose();
                    return Image.FromStream(ant2.Result);
                });

        }


        public static async Task<Image> DownloadImageFromUrlAsyncMethod(String url) {
            HttpClient client = new HttpClient();

            client.DefaultRequestHeaders.Add("User-Agent", "Mozilla/5.0");
            Debug.WriteLine("Start DownloadImageFromUrlAsyncMethod in thread {0}",
                Thread.CurrentThread.ManagedThreadId);

            Stream s = await client.GetStreamAsync(url); //.ConfigureAwait(false);

            Debug.WriteLine("after wait GetStreamAsync on DownloadImageFromUrlAsyncMethod in thread {0}",
                Thread.CurrentThread.ManagedThreadId);

            Stream ms = new MemoryStream();
            await s.CopyToAsync(ms); //.ConfigureAwait(false);

            Debug.WriteLine("after wait on CopyToAsync DownloadImageFromUrlAsyncMethod in thread {0}",
                Thread.CurrentThread.ManagedThreadId);
            return Image.FromStream(ms);
        }
    }
}
