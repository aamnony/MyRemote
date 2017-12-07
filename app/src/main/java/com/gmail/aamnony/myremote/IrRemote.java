package com.gmail.aamnony.myremote;

import java.util.Map;

public interface IrRemote
{
    CharSequence getName ();

    Map<String, int[]> getButtons ();

    int[] getPattern (String button);

    /* Possible TODO: Split getName() into getBrand() and getType().
                      e.g. getName() == "YesSilverRemote"
                           into:
                            getBrand() == "Yes"
                            getType()  == "Silver"
    */
}
