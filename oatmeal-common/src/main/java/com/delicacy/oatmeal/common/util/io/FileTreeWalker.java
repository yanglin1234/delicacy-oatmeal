package com.delicacy.oatmeal.common.util.io;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import com.google.common.collect.TreeTraverser;
import com.google.common.io.Files;

public class FileTreeWalker {

	/**
	 * 前序递归列出所有文件, 包含文件与目录，及根目录本身.
	 * 
	 * 前序即先列出父目录，在列出子目录. 如要后序遍历, 直接使用Files.fileTreeTraverser()
	 */
	public static List<File> listAll(File rootDir) {
		return Files.fileTreeTraverser().preOrderTraversal(rootDir).toList();
	}

	/**
	 * 前序递归列出所有文件, 只包含文件.
	 */
	public static List<File> listFile(File rootDir) {
		return Files.fileTreeTraverser().preOrderTraversal(rootDir).filter(Files.isFile()).toList();
	}

	/**
	 * 前序递归列出所有文件, 列出后缀名匹配的文件. （后缀名不包含.）
	 */
	public static List<File> listFileWithExtension(final File rootDir, final String extension) {
		return Files.fileTreeTraverser().preOrderTraversal(rootDir).filter(new FileExtensionFilter(extension)).toList();
	}


	/**
	 * 前序递归列出所有文件, 列出文件名匹配正则表达式的文件
	 * 
	 * 如 ("/a/b/hello.txt", "he.*\.text") 将被返回
	 */
	public static List<File> listFileWithRegexFileName(final File rootDir, final String regexFileNamePattern) {
		return Files.fileTreeTraverser().preOrderTraversal(rootDir)
				.filter(new RegexFileNameFilter(regexFileNamePattern)).toList();
	}

	/**
	 * 以文件名正则表达式为filter，配合fileTreeTraverser使用
	 */
	public static final class RegexFileNameFilter implements Predicate<File> {
		private final Pattern pattern;

		private RegexFileNameFilter(String pattern) {
			this.pattern = Pattern.compile(pattern);
		}

		@Override
		public boolean apply(File input) {
			return input.isFile() && pattern.matcher(input.getName()).matches();
		}
	}



	/**
	 * 以文件名后缀做filter，配合fileTreeTraverser使用
	 */
	public static final class FileExtensionFilter implements Predicate<File> {
		private final String extension;

		private FileExtensionFilter(String extension) {
			this.extension = extension;
		}

		@Override
		public boolean apply(File input) {
			return input.isFile() && extension.equals(FileUtil.getFileExtension(input));
		}
	}


	public static void main(String[] args) {
		System.out.println(listAll(new File("F:\\tools\\bat")));
		System.out.println(listFile(new File("F:\\tools\\bat")));
		System.out.println(listFileWithRegexFileName(new File("F:\\tools\\bat"),"^.*www.*$"));
		System.out.println(listFileWithExtension(new File("F:\\tools\\bat"),"rar" ));

	}
}
