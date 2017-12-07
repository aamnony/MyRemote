package com.gmail.aamnony.myremote;

public class TvChannel
{
    public final String name;
    public final int number;
    public final int[] digits;

    public TvChannel (String name, int number)
    {
        this.name = name;
        this.number = number;

        digits = getDigits(number);
    }

    @Override
    public String toString ()
    {
        return name;
    }

    public static int[] getDigits (int number)
    {
        int[] digits = new int[3];
        digits[2] = number / 100;
        int i = number % 100;
        digits[1] = i / 10;
        digits[0] = i % 10;
        return digits;
    }
}
