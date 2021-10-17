package org.adbs.dbxic.utils;

public class test_ByteConversion {

    public static void main (String args[]) {

        byte b[] = new byte[ByteConversion.LONG_SIZE*2560];
        for (int i = 0; i < 2560; i++)
            ByteConversion.toByte((long) i, b, i*ByteConversion.LONG_SIZE);

        for (int i = 0; i < 2560; i++)
            System.out.println(ByteConversion.toLong(b, i*ByteConversion.LONG_SIZE));

        b = new byte[8];
        //b = ByteConversion.toByte(0x47851f79);
        ByteConversion.toByte(0x47851f79, b, 0);
        System.out.println("int: " + 0x47851f79);
        for (int i = 0; i <= 3; i++)
            System.out.print("   " + b[i]);
        
        System.out.println();
        System.out.println("back to int: " + ByteConversion.toInt(b));
        System.out.println();
        b = ByteConversion.toByte((short)-177);
        System.out.println("short: " + -177);
        for (int j = 0; j <= 1; j++)
            System.out.print("   " + b[j]);
        
        System.out.println();
        System.out.println("back to short: " + ByteConversion.toShort(b));
        System.out.println();
        b = ByteConversion.toByte(0x48a749338441e818L);
        System.out.println("long: " + 0x48a749338441e818L);
        for (int k = 0; k <= 7; k++)
            System.out.print("   " + b[k]);
        
        System.out.println();
        System.out.println("back to long: " + ByteConversion.toLong(b));
        System.out.println();
        b = ByteConversion.toByte('k');
        System.out.println("char: " + 'k');
        for (int l = 0; l <= 1; l++)
            System.out.print("   " + b[l]);
        
        System.out.println();
        System.out.println("back to char: " + ByteConversion.toChar(b));
        System.out.println();
        b = ByteConversion.toByte(-564351.4F);
        System.out.println("float: " + -564351.4F);
        for (int i1 = 0; i1 <= 3; i1++)
            System.out.print("   " + b[i1]);
        
        System.out.println();
        System.out.println("back to float: " + ByteConversion.toFloat(b));
        System.out.println();
        b = ByteConversion.toByte(139245812345123.45D);
        System.out.println("double: " + 139245812345123.45D);
        for (int j1 = 0; j1 <= 7; j1++)
            System.out.print("   " + b[j1]);
        
        System.out.println();
        System.out.println("back to double: " + ByteConversion.toDouble(b));

    }
}
