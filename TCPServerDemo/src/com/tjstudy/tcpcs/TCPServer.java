package com.tjstudy.tcpcs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * �������ˣ�ʱ��׼�����ÿͻ������ӣ����յ��ͻ�������֮�󣬶�ȡ�ͻ��˷���������Ϣ�����������ݵ��ͻ���
 * 
 * @author Administrator
 * 
 */
public class TCPServer {
	private static Selector serverSelector;
	private static ByteBuffer serBuffer;

	public static void main(String[] args) throws IOException {
		// 1����ȡ�����Channel
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		// �������˰󶨵�ָ���Ķ˿�
		serverSocketChannel.socket().bind(new InetSocketAddress(8888));

		// 2����������� �ŵ�
		serverSelector = Selector.open();
		serverSocketChannel.configureBlocking(false);// must ����Ϊ������
		serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);// ��ʼ״̬������Ϊ��accept��
		serBuffer = ByteBuffer.allocate(1024);

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {// ���ѭ�� ��֤������һֱ��������״̬
					try {
						int select = serverSelector.select();
						if (select == 0) {
							// System.out.println("select:" + select);
							continue;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					// �ڲ���Ҫһֱ��ȡ�ŵ���ֵ ������Ӧ����
					Iterator<SelectionKey> sKeys = serverSelector
							.selectedKeys().iterator();
					while (sKeys.hasNext()) {
						SelectionKey key = sKeys.next();
						if (key.isAcceptable()) {
							System.out.println("key.isAcceptable() �ͻ�������");
							// ÿһ���ŵ����Դ�����channel
							ServerSocketChannel serverChannel = (ServerSocketChannel) key
									.channel();
							try {
								SocketChannel clientChannel = serverChannel
										.accept();
								// ���ͻ���Channel�󶨵��ŵ�
								clientChannel.configureBlocking(false);
								clientChannel.register(serverSelector,
										SelectionKey.OP_READ);// Ҫ��ȡ���������
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else if (key.isReadable()) {
							String s = "";
							// ��ȡ����
							serBuffer.clear();
							SocketChannel clientChannel = (SocketChannel) key
									.channel();
							try {
								int read = clientChannel.read(serBuffer);
								if (read == -1) {
									System.out.println("read==-1");
									continue;
								}
								serBuffer.flip();
								s = new String(serBuffer.array(), 0, serBuffer
										.limit());
								System.out.println("���������յ����ݣ�" + s);

								// �����ŵ�ģʽΪ��дģʽ
								clientChannel.configureBlocking(false);
								clientChannel.register(serverSelector,
										SelectionKey.OP_WRITE);
							} catch (IOException e) {
								try {
									clientChannel.close();
									key.cancel();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								// e.printStackTrace();
							}
						} else if (key.isWritable() && key.isValid()) {
							SocketChannel clientChannel = (SocketChannel) key
									.channel();
							serBuffer.clear();
							serBuffer.put("$$".getBytes());
							byte[] len = short2Byte(12);
							serBuffer.put(len);
							serBuffer.put("tjstudy".getBytes());
							serBuffer.flip();
							try {
								clientChannel.write(serBuffer);
								// �л��ɶ�ģʽ�����򽫻�һֱ��дģʽ
								clientChannel.configureBlocking(false);
								clientChannel.register(serverSelector,
										SelectionKey.OP_READ);// Ҫ��ȡ���������
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								try {
									clientChannel.close();
									key.cancel();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}
						sKeys.remove();
					}
				}
			}
		}).start();
	}

	/**
	 * ��shortת��Ϊbyte����
	 * 
	 * @param s
	 *            short
	 * @return
	 */
	private static byte[] short2Byte(int s) {
		byte[] shortBuf = new byte[2];
		for (int i = 0; i < 2; i++) {
			int offset = (shortBuf.length - 1 - i) * 8;
			shortBuf[1 - i] = (byte) ((s >>> offset) & 0xff);
		}
		return shortBuf;
	}
}
