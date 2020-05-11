package fr.cnrs.luc_hogie.fastcsv;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class FastCSV
{
	public static char sep = ',';
	
	public static long parseLong(InputStream in, long dv) throws IOException
	{
		long n = 0;
		int nbBytesRead = 0;
		boolean quoted = false;

		while (true)
		{
			int i = in.read();

			if (i == - 1)
			{
				if (nbBytesRead == 0)
					throw new EOFException();

				return n;
			}

			++nbBytesRead;

			if (Character.isDigit(i))
			{
				n = n * 10 + i - '0';
			}
			else if (i == '\r')
			{
			}
			else if (i == '"' && nbBytesRead == 1)
			{
				quoted = true;
			}
			else if (i == '-' && nbBytesRead == 1)
			{
				n = - 0;
			}
			else if (i == sep)
			{
				if (nbBytesRead == 1)
					return dv;

				return n;
			}
			else if (i == '"' && quoted)
			{
				// consume the , or \n
				in.read();
				return n;
			}
			else if (i == '\n' || i == '\r')
			{
				if (nbBytesRead == 1)
					return dv;

				return n;
			}
			else
			{
				throw new IllegalStateException(
						"invalid character in integer number: " + print(i));
			}
		}
	}



	private static String print(int i) {
		return i + " (" + ((char) i) + ")";
	}



	static byte[] boolB = new byte[20];

	static public boolean parseBoolean(InputStream in) throws IOException
	{
		int n = parseStringAsByteArray(in, boolB);

		if (n == 4 && boolB[0] == 't' && boolB[1] == 'r' && boolB[2] == 'u'
				&& boolB[3] == 'e')
		{
			return true;
		}
		else if (n == 5 && boolB[0] == 'f' && boolB[1] == 'a' && boolB[2] == 'l'
				&& boolB[3] == 's' && boolB[4] == 'e')
		{
			return false;
		}
		else
		{
			throw new IllegalStateException(
					"invalid boolean: '" + new String(boolB, 0, n) + "'");
		}
	}

	public static String parseString(InputStream in)
			throws IOException
	{
		byte[] b = new byte[500];
		int n = parseStringAsByteArray(in, b);
		return new String(b, 0, n);
	}
	
	public static byte[] parseBytes(InputStream in)
			throws IOException
	{
		byte[] b = new byte[500];
		int n = parseStringAsByteArray(in, b);
		return Arrays.copyOf(b, n);
	}

	public static int parseStringAsByteArray(InputStream in, byte[] b)
			throws IOException
	{
		int n = 0;
		int size = 0;
		boolean quoted = false;

		while (true)
		{
			int i = in.read();

			if (i == - 1)
			{
				return size;
			}

			++n;

			if (i == sep && ! quoted)
			{
				return size;
			}
			else if (i == '"' && n == 1)
			{
				quoted = true;
			}
			else if (i == '"' && quoted)
			{
				// consume the ,
				in.read();
				return size;
			}
			else if (i == '\n' && ! quoted)
			{
				return size;
			}
			else
			{
				b[size++] = (byte) i;
			}
		}
	}
}
