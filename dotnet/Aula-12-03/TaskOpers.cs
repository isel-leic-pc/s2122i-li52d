using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;

namespace Aula_12_03
{
    class TaskOpers
    {

        /// <summary>
        /// Incomplete implementation of an async method to
        /// download and save http response content on a file
        /// </summary>
        /// <param name="url"></param>
        /// <param name="fileName"></param>
        /// <returns></returns>
        public static Task<bool> DownloadToFileAsync(String url, String fileName) {
            HttpClient client = new HttpClient();
            client.DefaultRequestHeaders.Add("User-Agent", "Mozilla/5.0");

            Task<Stream> task = client.GetAsync(url)
            .ContinueWith((ant) =>
            {
                
                HttpResponseMessage resp = ant.Result;
                return resp.Content.ReadAsStreamAsync();
            })
            .Unwrap();
            

            return  null;
                  
        }
    }
}
