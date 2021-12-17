using System;
using System.Collections.Generic;
using System.Text;

namespace Aula_12_15_AsyncMethods
{
    public class Iterators
    {

        public static IEnumerable<int> Evens() {
            int num = 0;
            while (true) {
                num += 2;
                yield return num;
            }

        }
    }
}
