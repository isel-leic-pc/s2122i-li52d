using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;

namespace Aula_12_03
{
    public class TaskOpers
    {

        /// <summary>
        /// Incomplete implementation of a TAP method to
        /// download and save http response content on a file.
        /// The completed version is presented in class TaskUtils of Aula-12-10-AyncIntro2 project
        /// </summary>
        /// <param name="url"></param>
        /// <param name="fileName"></param>
        /// <returns></returns>
        public static Task<bool> DownloadToFileAsync(String url, String fileName) {
            HttpClient client = new HttpClient();
            client.DefaultRequestHeaders.Add("User-Agent", "Mozilla/5.0");

            Task<HttpResponseMessage> task = client.GetAsync(url);

            task.ContinueWith((ant) =>
            {
                 //throw new IOException("Bad request");
                 HttpResponseMessage resp = ant.Result;
                return resp.Content.ReadAsStreamAsync();
            })
            .Unwrap();

            return null;
        }
    }
}
