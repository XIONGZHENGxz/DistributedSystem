public class Merge implements Runnable {

    int[] firstArray;
    int[] secondArray;
    int[] result;

    Boolean looking_at_A;
    int index;

    int dupFlag = 0;

    public Merge(int[] a, int[] b, int[] c, Boolean AorNah, int index) {

        this.firstArray = a;
        this.secondArray = b;
        this.result = c;

        this.looking_at_A = AorNah;
        this.index = index;
    }


    int binarySearch(int targetVal, int[] sortArr) {

        int left = 0;
        int right = sortArr.length - 1;

        while (left <= right) {

            int mid = (left + right) / 2;

            if (sortArr[mid] == targetVal) {

                dupFlag = 1;
                return mid;
            }

            else if (sortArr[mid] > targetVal) {

                right = mid - 1;
            }

            else left = mid + 1;
        }

        return left;
    }


    @Override
    public void run() {

        if (looking_at_A == true) {

            int indexValue = firstArray[index];
            int cIndex = binarySearch(indexValue, secondArray) + index + dupFlag;

            result[cIndex] = indexValue;
        }

        if (looking_at_A == false) {

            int indexValue = secondArray[index];
            int cIndex = binarySearch(indexValue, firstArray) + index;

            result[cIndex] = indexValue;
        }
    }
}
