package com.gmail.aamnony.myremote;

import java.util.Map;

public final class PilotTvSilverRemote implements IrRemote
{
    public PilotTvSilverRemote ()
    {
    }

    @Override
    public CharSequence getName ()
    {
        return "Pilot TV Silver Remote (GCBLTV11A-C4)";
    }


    @Override
    public int[] getPattern (String button)
    {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Map<String, int[]> getButtons ()
    {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
