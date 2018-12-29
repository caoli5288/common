package com.mengcraft.util;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class VarIntDataStreamTest {

    @Test
    public void writeString() {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        VarIntDataStream.writeString(output, "hahaha");
        ByteArrayDataInput input = ByteStreams.newDataInput(output.toByteArray());
        Assert.assertEquals("hahaha", VarIntDataStream.readString(input));
    }
}