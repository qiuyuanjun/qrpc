package com.qiuyj.qrpc.test;

import com.qiuyj.qrpc.server.RpcServer;
import com.qiuyj.qrpc.server.RpcServerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.Stack;
import java.util.StringJoiner;

/**
 * @author qiuyj
 * @since 2020-03-14
 */
public class RpcServerTest {

    public static void main(String[] args) {
        RpcServer server = RpcServerFactory.createDefault();
        server.start();
        /*System.out.println(solve("UR11645E64O45CACC1GR1560C303X1A24CDCBYLX1616D491I"));
        System.out.println(MissingBrackets("()()()()(((())))?)(((())))()((()()()))"));
        System.out.println(charMove("*c*m*b*n*t"));
        // **cm*b*n*t;
        int[] arr = {6, 0, 10, 14, 56, -1, 9, 3, 0, -21, 2, 10, 32, 1, -100, 21, -500, -1, 200, 12, 0, 1, 3, 4, 10};
        quickSort(arr);
        System.out.println(Arrays.toString(arr));*/
    }

    public static void quickSort(int[] arr) {
        doQuickSort(arr, 0, arr.length - 1);
    }

    private static void doQuickSort(int[] arr, int low, int high) {
        if (low < high) {
            int middle = getMiddle(arr, low, high);
            doQuickSort(arr, low, middle - 1);
            doQuickSort(arr, middle + 1, high);
        }
    }

    private static int getMiddle(int[] arr, int low, int high) {
        int pivot = arr[low];
        while (low < high) {
            while (arr[high] > pivot) {
                high--;
            }
            arr[low] = arr[high];
            while (low < high && arr[low] <= pivot) {
                low++;
            }
            arr[high] = arr[low];
        }
        arr[low] = pivot;
        return low;
    }

    private static String charMove(String s) {
        char[] c = s.toCharArray();
        int last = 0;
        for (int i = 0; i < c.length; i++) {
            char cur = s.charAt(i);
            if (cur == '*') {
                int loop = i - last;
                int z = i;
                int j = 1;
                while (loop > 0) {
                    c[z] = c[i - j];
                    c[i - j] = '*';
                    z--;
                    j++;
                    loop--;
                }
                last++;
            }
        }
        return new String(c);
    }

    private static int solve(String s) {
        int len = s.length(), start = 0, end = 0;
        String lastHexString = null;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                end++;
            }
            else {
                if (start != end) {
                    String cur = s.substring(start, Math.min(end, len));
                    if (Objects.nonNull(lastHexString)
                            && Integer.parseInt(cur, 16) < Integer.parseInt(lastHexString, 16)) {
                        cur = lastHexString;
                    }
                    lastHexString = cur;
                }
                start = end = i + 1;
            }
        }
        if (Objects.isNull(lastHexString)) {
            lastHexString = s.substring(start, end < len ? end + 1 : len);
        }
        return Integer.parseInt(lastHexString, 16);
    }

    public static String MissingBrackets(String brackets) {
        // write code here
        Stack<Character> stack = new Stack<>();
        int len = brackets.length();
        int leftBracketsNum = 0;
        for (int i = 0; i < len; i++) {
            char c = brackets.charAt(i);
            if (stack.isEmpty()) {
                leftBracketsNum++;
            }
            else {
                if (c == '(') {
                    leftBracketsNum++;
                }
                else if (c == ')') {
                    if (leftBracketsNum != 0) {
                        leftBracketsNum--;
                    }
                }
                else {
                    // ?
                    if (leftBracketsNum == 0) {
                        c = '(';
                        leftBracketsNum++;
                    }
                    else {
                        c = ')';
                        leftBracketsNum--;
                    }
                }
            }
            stack.push(c);
        }
        if (leftBracketsNum != 0) {
            return "Impossible";
        }
        StringJoiner joiner = new StringJoiner("");
        stack.forEach(c -> joiner.add(String.valueOf(c)));
        return joiner.toString();
    }
}
