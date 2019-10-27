/*   1:    */ package org.textbug.utility;
/*   2:    */ 
/*   3:    */ import java.io.BufferedReader;
/*   4:    */ import java.io.BufferedWriter;
/*   5:    */ import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
/*   6:    */ import java.io.FileReader;
/*   7:    */ import java.io.FileWriter;
/*   8:    */ import java.io.FilenameFilter;
/*   9:    */ import java.io.IOException;
/*  10:    */ import java.io.InputStream;
/*  11:    */ import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
/*  12:    */ import java.io.PrintStream;
/*  13:    */ import java.util.ArrayList;
/*  14:    */ import java.util.HashMap;
/*  15:    */ import java.util.List;
/*  16:    */ import org.apache.log4j.Logger;
/*  17:    */ 
/*  18:    */ public class FileUtil
/*  19:    */ {
/*  20: 27 */   private static Logger logger = Logger.getLogger(FileUtil.class);
/*  21:    */   
/*  22:    */   public static synchronized void deleteFiles(String directory)
/*  23:    */   {
/*  24: 30 */     File f1 = new File(directory);
/*  25: 32 */     if (f1.isDirectory())
/*  26:    */     {
/*  27: 33 */       List<String> l = getFilesInDirectory(directory);
/*  28: 34 */       for (String file : l)
/*  29:    */       {
/*  30: 36 */         boolean success = new File(file).delete();
/*  31: 37 */         if (!success) {
/*  32: 38 */           logger.warn("Problem deleting" + file);
/*  33:    */         } else {
/*  34: 40 */           logger.info("Success deleting" + file);
/*  35:    */         }
/*  36:    */       }
/*  37:    */     }
/*  38:    */   }
/*  39:    */   
/*  40:    */   public static List getChildDirectories(String dirName)
/*  41:    */   {
/*  42: 55 */     List<String> files = new ArrayList();
/*  43: 56 */     File dir = new File(dirName);
/*  44:    */     
/*  45: 58 */     String[] children = dir.list();
/*  46: 59 */     if (children != null) {
/*  47: 62 */       for (int i = 0; i < children.length; i++)
/*  48:    */       {
/*  49: 64 */         String filename = dirName + System.getProperty("file.separator") + children[i];
/*  50: 65 */         if (new File(filename).isDirectory()) {
/*  51: 66 */           files.add(filename);
/*  52:    */         }
/*  53:    */       }
/*  54:    */     }
/*  55: 92 */     return files;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public static String getDirectoryOfFile(String file)
/*  59:    */   {
/*  60:102 */     return new File(file).getParent();
/*  61:    */   }
/*  62:    */   
/*  63:    */   public static List getFilesInDirectory(String dirName)
/*  64:    */   {
/*  65:110 */     List<String> files = new ArrayList();
/*  66:111 */     File dir = new File(dirName);
/*  67:    */     
/*  68:113 */     String[] children = dir.list();
/*  69:114 */     if (children != null) {
/*  70:117 */       for (int i = 0; i < children.length; i++)
/*  71:    */       {
/*  72:119 */         String filename = children[i];
/*  73:120 */         files.add(dirName + System.getProperty("file.separator") + filename);
/*  74:    */       }
/*  75:    */     }
/*  76:145 */     return files;
/*  77:    */   }
/*  78:    */   
/*  79:    */   public static List getFilesInDirectory(String dirName, String fileType)
/*  80:    */   {
/*  81:152 */     List<String> files = new ArrayList();
/*  82:    */     
/*  83:    */ 
/*  84:    */ 
/*  85:    */ 
/*  86:    */ 
/*  87:    */ 
/*  88:    */ 
/*  89:    */ 
/*  90:    */ 
/*  91:    */ 
/*  92:    */ 
/*  93:    */ 
/*  94:    */ 
/*  95:    */ 
/*  96:    */ 
/*  97:    */ 
/*  98:169 */     File dir = new File(dirName);
/*  99:170 */     FilenameFilter filter = new FilenameFilter()
/* 100:    */     {
/* 101:    */       public boolean accept(File dir, String name)
/* 102:    */       { 
/* 103:172 */         if (name.endsWith("FileUtil.this")) {
/* 104:173 */           return true;
/* 105:    */         }
/* 106:174 */         return false;
/* 107:    */       }
/* 108:176 */     };
/* 109:177 */     String[] children = dir.list(filter);
/* 110:179 */     for (int i = 0; i < children.length; i++)
/* 111:    */     {
/* 112:181 */       String filename = children[i];
/* 113:182 */       files.add(dirName + System.getProperty("file.separator") + filename);
/* 114:    */     }
/* 115:193 */     return files;
/* 116:    */   }
/* 117:    */   
/* 118:    */   public static synchronized BufferedReader getReader(String strAbsPathToFile)
/* 119:    */     throws IOException
/* 120:    */   {
/* 121:203 */     BufferedReader br = null;
/* 122:204 */     //br = new BufferedReader(new FileReader(strAbsPathToFile));
File fileDir = new File(strAbsPathToFile);
br = new BufferedReader( new InputStreamReader(new FileInputStream(fileDir), "UTF8"));
/* 123:205 */     return br;
/* 124:    */   }
/* 125:    */   
/* 126:    */   public static synchronized BufferedReader getResourceFromPackage(String strAbsPathToFile)
/* 127:    */     throws Exception
/* 128:    */   {
/* 129:217 */     BufferedReader br = null;
/* 130:218 */     ClassLoader classLoader = Thread.currentThread()
/* 131:219 */       .getContextClassLoader();
/* 132:220 */     InputStream is = classLoader.getResourceAsStream(strAbsPathToFile);
/* 133:222 */     if (is == null) {
/* 134:223 */       throw new Exception("File '" + strAbsPathToFile + 
/* 135:224 */         "' is missing in classpath.");
/* 136:    */     }
/* 137:227 */     br = new BufferedReader(new InputStreamReader(is));
/* 138:    */     
/* 139:229 */     return br;
/* 140:    */   }
/* 141:    */   
/* 142:    */   public static synchronized BufferedWriter getWriter(String strFile)
/* 143:    */     throws IOException
/* 144:    */   {
/* 145:234 */     BufferedWriter br = null;
/* 146:235 */     //br = new BufferedWriter(new FileWriter(strFile));
File fileDir = new File(strFile);
br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), "UTF-8"));
/* 147:236 */     return br;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public static boolean isFileExists(String file)
/* 151:    */   {
/* 152:245 */     return new File(file).exists();
/* 153:    */   }
/* 154:    */   
/* 155:    */   public static void createDir(String dir)
/* 156:    */   {
/* 157:249 */     File f = new File(dir);
/* 158:250 */     if (!f.exists()) {
/* 159:251 */       f.mkdirs();
/* 160:    */     }
/* 161:    */   }
/* 162:    */   
/* 163:    */   public static void parseFileName(String fileName, String delimeter, HashMap<String, String> map)
/* 164:    */   {
/* 165:269 */     int idx = fileName.lastIndexOf('.');
/* 166:270 */     if (idx > 0) {
/* 167:271 */       String str1 = fileName.substring(idx, fileName.length());
/* 168:    */     }
/* 169:275 */     fileName = fileName.substring(0, idx);
/* 170:    */     
/* 171:277 */     String[] nvPairs = fileName.split(delimeter);
/* 172:278 */     if (nvPairs.length > 0) {
/* 173:281 */       for (int i = 0; i < nvPairs.length; i++)
/* 174:    */       {
/* 175:282 */         idx = nvPairs[i].indexOf('=');
/* 176:284 */         if (idx < 0) {
/* 177:285 */           System.err.println(fileName);
/* 178:    */         }
/* 179:288 */         String name = nvPairs[i].substring(0, nvPairs[i].indexOf('='));
/* 180:289 */         String value = nvPairs[i].substring(nvPairs[i].indexOf('=') + 1, nvPairs[i].length());
/* 181:    */         
/* 182:291 */         map.put(name, value);
/* 183:    */       }
/* 184:    */     }
/* 185:    */   }
/* 186:    */ }


