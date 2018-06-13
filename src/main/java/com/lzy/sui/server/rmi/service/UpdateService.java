package com.lzy.sui.server.rmi.service;

import java.io.File;
import java.io.RandomAccessFile;

import com.lzy.sui.common.inf.UpdateInf;

public class UpdateService implements UpdateInf{

//	private final String CORPSEPATH="C:\\Users\\Sol】随\\suicls\\sui.jar";
	private final String CORPSEPATH="/opt/sui_server/update/sui.jar";
	
	@Override
	public Long getCorpseLastestSize() {
		File file=new File(CORPSEPATH);
		return file.length();
	}

	@Override
	public byte[] getCorpseUpdatePart(int partSize, int partNum) {
		byte[] bytes = new byte[partSize];
		try {
			File file = new File(CORPSEPATH);
			int startIndex = (partNum - 1) * partSize;
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(startIndex);
			int num = raf.read(bytes);
			if (num != partSize) {
				byte[] copyBytes = new byte[num];
				System.arraycopy(bytes, 0, copyBytes, 0, num);
				bytes = copyBytes;
			}
			System.out.println("下载Corpse jar更新包：" + CORPSEPATH + " 第" + partNum + "部分完成");
			raf.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("下载Corpse jar更新包异常，文件路径：" + CORPSEPATH + " 大小：" + partSize + " 序号：" + partNum);
		}
		return bytes;
	}

}
