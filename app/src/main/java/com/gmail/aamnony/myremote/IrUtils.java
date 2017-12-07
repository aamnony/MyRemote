package com.gmail.aamnony.myremote;

/**
 * @deprecated see {@link IrRemote}.
 */
@Deprecated
public class IrUtils
{

    /**
     * Extracts the bits of {@code value}, from right (lsb) to left (msb).
     * {@code value} must be non-negative.
     *
     * @param value the number to extract the bits from.
     * @return An {@code int} array containing the bits, first element is msb, last element is lsb.
     * @throws IllegalArgumentException if {@code value} is negative.
     */
    @Deprecated
    public static int[] toBitsArray (long value)
    {
        if (value < 0)
        {
            throw new IllegalArgumentException("Argument 'value' must be non-negative");
        }
        if (value == 0)
        {
            return new int[]{0};
        }
        int[] array = new int[Long.SIZE];
        int i = array.length;
        while (value != 0)
        {
            array[--i] = (int) (value & 1);
            value = value >> 1;
        }
        int[] trimmedArray = new int[array.length - i];
        System.arraycopy(array, i, trimmedArray, 0, trimmedArray.length);
        return trimmedArray;
    }
}
