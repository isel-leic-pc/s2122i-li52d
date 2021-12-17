using System;
using System.Threading.Tasks;

namespace Aula_12_16_AsyncMethodsAndAsynchronizers
{
    class Program
    {
        static void Main(string[] args) {
            Task<int> taskInt = TaskUtils3.ConsumeIntSupplierAsync();

            try {
                Console.WriteLine(taskInt.Result);
            }
            catch(Exception e) {
                Console.WriteLine("exception type : {0}", e.GetType());
                Console.WriteLine("exception msg : {0}", e.Message);
            }
            
        }
    }
}
