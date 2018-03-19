/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter;

import java.net.Inet4Address;

public class IPv4Address extends IPAddress implements Comparable<IPv4Address> {

	public static enum IPv4KnownRange {

		//@formatter:off
		/**
		 * 127.0.0.0/8 - RFC 990
		 */
		LOOPBACK(IPv4Subnet.of("127.0.0.0/8")),
		/**
		 * 0.0.0.0/8 - RFC 1700
		 */
		UNSPECIFIED(IPv4Subnet.of("0.0.0.0/8")),
		/**
		 * 10.0.0.0/8 - RFC 1918
		 */
		PRIVATE_10(IPv4Subnet.of("10.0.0.0/8")),
		/**
		 * 172.16.0.0/12 - RFC 1918
		 */
		PRIVATE_172_16(IPv4Subnet.of("172.16.0.0/12")),
		/**
		 * 192.168.0.0/16 - RFC 1918
		 */
		PRIVATE_192_168(IPv4Subnet.of("192.168.0.0/16")),
		/**
		 * 198.18.0.0/15 - RFC 2544
		 */
		TESTING(IPv4Subnet.of("198.18.0.0/15")),
		/**
		 * 192.88.99.0/24 - RFC 3068
		 */
		TRANSLATION_6_TO_4(IPv4Subnet.of("192.88.99.0/24")),
		/**
		 * 169.254.0.0/16 - RFC 3927
		 */
		LINK_LOCAL(IPv4Subnet.of("169.254.0.0/16")),
		/**
		 * 192.0.0.0/24 - RFC 5736
		 */
		SPECIAL_PURPOSE(IPv4Subnet.of("192.0.0.0/24")),
		/**
		 * 192.0.2.0/24 - RFC 5737
		 */
		TEST_NET1(IPv4Subnet.of("192.0.2.0/24")),
		/**
		 * 198.51.100.0/24 - RFC 5737
		 */
		TEST_NET2(IPv4Subnet.of("198.51.100.0/24")),
		/**
		 * 203.0.113.0/24 - RFC 5737
		 */
		TEST_NET3(IPv4Subnet.of("203.0.113.0/24")),
		/**
		 * 224.0.0.0/4 - RFC 5771
		 */
		MULTICAST(IPv4Subnet.of("224.0.0.0/4")),
		/**
		 * "100.64.0.0/10 - RFC 6598
		 */
		CGNAT(IPv4Subnet.of("100.64.0.0/10")),
		/**
		 * 240.0.0.0/4 - RFC 6890
		 */
		RESERVED_240(IPv4Subnet.of("240.0.0.0/4")),
		/**
		 * 255.255.255.255/32 - RFC 6890
		 */
		BROADCAST(IPv4Subnet.of("255.255.255.255/32"));
		//@formatter:on

		private IPv4Range range;

		private IPv4KnownRange(IPv4Range range) {
			this.range = range;
		}

		public boolean contains(IPv4Address address) {
			return this.range.contains(address);
		}

		public IPv4Range range() {
			return this.range;
		}
	}

	static enum Ip4Octet {
		OCTET_A(0), OCTET_B(1), OCTET_C(2), OCTET_D(3);

		private final int mask;
		private final int shift;

		private Ip4Octet(int byteShift) {
			this.shift = 24 - (byteShift << 3);
			this.mask = 0xff000000 >>> (byteShift << 3);
		}

		public int isolateAsInt(int ip) {
			return (ip & this.mask) >>> this.shift;
		}

		public byte isolateAsByte(int ip) {
			return (byte) isolateAsInt(ip);
		}
	}

	private static final long serialVersionUID = 1L;

	public static IPv4Address of(byte[] bigEndianByteArr) {
		return new IPv4Address(bigEndianByteArr);
	}

	public static IPv4Address of(int intIp) {
		return new IPv4Address(intIp);
	}

	public static IPv4Address of(IPv4Address other) {
		return new IPv4Address(other.ip);
	}

	public static IPv4Address of(String ip) {
		if (ip == null) {
			throw new NullPointerException("String IP address is null");
		}
		if (ip.length() < 7 || ip.length() > 15) {
			throw new IllegalArgumentException("Invalid IP address length");
		}
		String[] split = ip.split("\\.");
		if (split.length != 4) {
			throw new IllegalArgumentException("IPv4 addresses must have exactly 4 octets");
		}
		int a = Integer.parseInt(split[0]), b = Integer.parseInt(split[1]);
		int c = Integer.parseInt(split[2]), d = Integer.parseInt(split[3]);

		// Make sure all octets are between 0 and 255
		if (((a | b | c | d) & 0xffffff00) != 0) {
			throw new IllegalArgumentException("All octets have to be between 0 and 255");
		}

		return of(a << 24 | b << 16 | c << 8 | d);
	}

	public static IPv4Address of(Inet4Address address) {
		return of(address.getAddress());
	}

	// Instance variables and methods

	final int ip;

	IPv4Address(byte[] bigEndianByteArr) {
		if (bigEndianByteArr == null) {
			throw new NullPointerException("The given array is null");
		}
		if (bigEndianByteArr.length != 4) {
			throw new IllegalArgumentException(
					String.format("The array has to be 4 bytes long, the given array is %d bytes long",
							Integer.valueOf(bigEndianByteArr.length)));
		}

		this.ip = (bigEndianByteArr[0] & 0xff) << 24 | (bigEndianByteArr[1] & 0xff) << 16
				| (bigEndianByteArr[2] & 0xff) << 8 | (bigEndianByteArr[3] & 0xff);
	}

	IPv4Address(int intIp) {
		this.ip = intIp;
	}

	@Override
	public int compareTo(IPv4Address o) {
		if (o == null) {
			return 1;
		}

		if (this.ip == o.ip) {
			return 0;
		}
		return (this.ip + Integer.MIN_VALUE) < (o.toInt() + Integer.MIN_VALUE) ? -1 : 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof IPv4Address)) {
			return false;
		}
		return ((IPv4Address) obj).ip == this.ip;
	}

	@Override
	public int hashCode() {
		return this.ip;
	}

	@Override
	public boolean is6To4() {
		return IPv4KnownRange.TRANSLATION_6_TO_4.contains(this);
	}

	public boolean isBroadcast() {
		return IPv4KnownRange.BROADCAST.contains(this);
	}

	@Override
	public boolean isMartian() {
		return isReserved() || isPrivate() || is6To4() || isBroadcast() || isLinkLocal() || isMulticast()
				|| isLoopback() || isUnspecified();
	}

	@Override
	public boolean isLinkLocal() {
		return IPv4KnownRange.LINK_LOCAL.contains(this);
	}

	@Override
	public boolean isLoopback() {
		return IPv4KnownRange.LOOPBACK.contains(this);
	}

	@Override
	public boolean isMulticast() {
		return IPv4KnownRange.MULTICAST.contains(this);
	}

	@Override
	public boolean isPrivate() {
		return IPv4KnownRange.PRIVATE_10.contains(this) || IPv4KnownRange.PRIVATE_172_16.contains(this)
				|| IPv4KnownRange.PRIVATE_192_168.contains(this) || IPv4KnownRange.CGNAT.contains(this);

	}

	@Override
	public boolean isReserved() {
		return IPv4KnownRange.RESERVED_240.contains(this) || IPv4KnownRange.SPECIAL_PURPOSE.contains(this)
				|| IPv4KnownRange.TEST_NET1.contains(this) || IPv4KnownRange.TEST_NET2.contains(this)
				|| IPv4KnownRange.TEST_NET3.contains(this) || IPv4KnownRange.TESTING.contains(this);
	}

	@Override
	public boolean isUnspecified() {
		return IPv4KnownRange.UNSPECIFIED.contains(this);
	}

	@Override
	public IPv4Address next() {
		return plus(1);
	}

	@Override
	public IPv4Address plus(int n) {
		return new IPv4Address((int) (toLong() + n));
	}

	@Override
	public IPv4Address previous() {
		return minus(1);
	}

	@Override
	public IPv4Address minus(int n) {
		return new IPv4Address((int) (toLong() - n));
	}

	@Override
	public byte[] toBigEndianArray() {
		return new byte[] { Ip4Octet.OCTET_A.isolateAsByte(this.ip), Ip4Octet.OCTET_B.isolateAsByte(this.ip),
				Ip4Octet.OCTET_C.isolateAsByte(this.ip), Ip4Octet.OCTET_D.isolateAsByte(this.ip) };
	}

	public Inet4Address toInet4Address() {
		return (Inet4Address) toInetAddress();
	}

	public int toInt() {
		return this.ip;
	}

	@Override
	public byte[] toLittleEndianArray() {
		return new byte[] { Ip4Octet.OCTET_D.isolateAsByte(this.ip), Ip4Octet.OCTET_C.isolateAsByte(this.ip),
				Ip4Octet.OCTET_B.isolateAsByte(this.ip), Ip4Octet.OCTET_A.isolateAsByte(this.ip) };
	}

	@Override
	public String toString() {
		return String.join(".", Integer.toString(Ip4Octet.OCTET_A.isolateAsInt(this.ip)),
				Integer.toString(Ip4Octet.OCTET_B.isolateAsInt(this.ip)),
				Integer.toString(Ip4Octet.OCTET_C.isolateAsInt(this.ip)),
				Integer.toString(Ip4Octet.OCTET_D.isolateAsInt(this.ip)));
	}

	public long toLong() {
		return this.ip & 0x00000000ffffffffL;
	}

	@Override
	public int version() {
		return 4;
	}
}