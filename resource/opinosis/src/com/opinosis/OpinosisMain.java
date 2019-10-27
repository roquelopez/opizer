/*   1:    */ package com.opinosis;
/*   2:    */ 
/*   3:    */ import com.opinosis.summarizer.BasicSummarizer;
/*   4:    */ import java.io.BufferedReader;
/*   5:    */ import java.io.BufferedWriter;
/*   6:    */ import java.io.File;
/*   7:    */ import java.io.FileInputStream;
/*   8:    */ import java.io.FileReader;
/*   9:    */ import java.io.IOException;
/*  10:    */ import java.io.InputStream;
import java.io.InputStreamReader;
/*  11:    */ import java.io.PrintStream;
/*  12:    */ import java.io.PrintWriter;
/*  13:    */ import java.io.Writer;
/*  14:    */ import java.util.ArrayList;
/*  15:    */ import java.util.HashMap;
/*  16:    */ import java.util.List;
/*  17:    */ import java.util.Properties;


import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.textbug.utility.FileUtil;
/*  23:    */ 
/*  24:    */ public class OpinosisMain
/*  25:    */   extends OpinosisSettings
/*  26:    */ {
/*  27: 37 */   String strRundId = "";
/*  28: 38 */   Properties properties = new Properties();
/*  29:    */   
/*  30:    */   public void loadProps(String propfile)
/*  31:    */   {
/*  32:    */     try
/*  33:    */     {
/*  34: 42 */       InputStream stream = new FileInputStream(propfile);
/*  35: 43 */       this.properties.load(stream);
/*  36:    */       
/*  37: 45 */       String property = this.properties.getProperty("collapse", "true");
/*  38: 46 */       CONFIG_TURN_ON_COLLAPSE = Boolean.parseBoolean(property);
/*  39:    */       
/*  40: 48 */       property = this.properties.getProperty("dupelim", "true");
/*  41: 49 */       CONFIG_TURN_ON_DUP_ELIM = Boolean.parseBoolean(property);
/*  42:    */       
/*  43: 51 */       property = this.properties.getProperty("normalized", "true");
/*  44: 52 */       CONFIG_NORMALIZE_OVERALLGAIN = Boolean.parseBoolean(property);
/*  45:    */       
/*  46: 54 */       property = this.properties.getProperty("redundancy", "2");
/*  47: 55 */       CONFIG_MIN_REDUNDANCY = Integer.parseInt(property);
/*  48:    */       
/*  49: 57 */       property = this.properties.getProperty("scoring_function", String.valueOf(GAIN_WEIGHTED_REDUNDANCY_BY_LOG_LEVEL));
/*  50: 58 */       CONFIG_SCORING_FUNCTION = Integer.parseInt(property);
/*  51:    */       
/*  52: 60 */       property = this.properties.getProperty("gap", "3");
/*  53: 61 */       CONFIG_PERMISSABLE_GAP = Integer.parseInt(property);
/*  54: 64 */       if (CONFIG_PERMISSABLE_GAP < 2) {
/*  55: 64 */         CONFIG_PERMISSABLE_GAP = 3;
/*  56:    */       }
/*  57: 66 */       property = this.properties.getProperty("attach_after", "2");
/*  58: 67 */       CONFIG_ATTACHMENT_AFTER = Integer.parseInt(property);
/*  59:    */       
/*  60: 69 */       property = this.properties.getProperty("duplicate_threshold", "0.35");
/*  61: 70 */       CONFIG_DUPLICATE_THRESHOLD = Double.parseDouble(property);
/*  62:    */       
/*  63: 72 */       property = this.properties.getProperty("max_summary", "2");
/*  64: 73 */       CONFIG_MAX_SUMMARIES = Integer.parseInt(property);
/*  65:    */       
/*  66: 75 */       property = this.properties.getProperty("collapse_duplicate_threshold", "0.5");
/*  67: 76 */       CONFIG_DUPLICATE_COLLAPSE_THRESHOLD = Double.parseDouble(property);
/*  68:    */       
/*  69: 78 */       property = this.properties.getProperty("run_id", "1");
/*  70: 79 */       this.strRundId = property;
/*  71:    */     }
/*  72:    */     catch (IOException exception)
/*  73:    */     {
/*  74: 83 */       exception.printStackTrace();
/*  75:    */     }
/*  76:    */   }
/*  77:    */   
/*  78:    */   public static void main(String[] args)
/*  79:    */   {
	String[]args1 = {"-b", "resource"};
/*  80: 88 */     OpinosisMain main = new OpinosisMain();
/*  81: 89 */     main.start(args1);
/*  82:    */   }
/*  83:    */   
/*  84:    */   private void start(String[] args)
/*  85:    */   {
	
/*  86: 96 */     MyOptions bean = new MyOptions();
/*  87: 97 */     CmdLineParser parser = new CmdLineParser(bean);
/*  88:    */     try
/*  89:    */     {
/*  90: 99 */       parser.parseArgument(args);
/*  91:101 */       if (bean.getDirBase() == null)
/*  92:    */       {
/*  93:102 */         parser.printUsage(System.err);
/*  94:103 */         System.exit(-1);
/*  95:    */       }
/*  96:    */     }
/*  97:    */     catch (CmdLineException e)
/*  98:    */     {
/*  99:107 */       System.err.println(e.getMessage() + "\n\n");
/* 100:108 */       System.err.println("java -jar opinosis.jar [options...] arguments...");
/* 101:109 */       parser.printUsage(System.err);
/* 102:110 */       return;
/* 103:    */     }
/* 104:114 */     long tstart = System.currentTimeMillis();
/* 105:    */     
/* 106:    */ 
/* 107:117 */     String propFile = bean.getDirBase().getAbsolutePath() + FILE_SEP + "etc" + FILE_SEP + "opinosis.properties";
/* 108:118 */     String inputDir = bean.getDirBase().getAbsolutePath() + FILE_SEP + "input";
/* 109:119 */     String outputDir = bean.getDirBase().getAbsolutePath() + FILE_SEP + "output";
/* 110:    */     
/* 111:    */ 
/* 112:122 */     loadProps(propFile);
/* 113:    */     
/* 114:    */ 
/* 115:125 */     List<String> filesToSum = new ArrayList();
/* 116:126 */     if ((inputDir.length() > 0) && (FileUtil.isFileExists(inputDir))) {
/* 117:127 */       filesToSum = FileUtil.getFilesInDirectory(inputDir);
/* 118:    */     } else {
/* 119:129 */       System.err.println(inputDir + " " + " does not exist..please check your directory structure");
/* 120:    */     }
/* 121:132 */     if (filesToSum.size() > 1000)
/* 122:    */     {
/* 123:133 */       System.err.println("Too many files to summarize. Please limit to 200 files per run.");
/* 124:134 */       System.exit(-1);
/* 125:    */     }
/* 126:138 */     int i = 1;
/* 127:139 */     for (String infile : filesToSum)
/* 128:    */     {
/* 129:140 */       String outfile = getOutputFileName(outputDir, infile);
/* 130:141 */       doGenerateSummary(infile, outfile, i++);
/* 131:    */     }
/* 132:143 */     long tend = System.currentTimeMillis();
/* 133:144 */     System.out.println("Took " + (tend - tstart) + "ms");
/* 134:    */   }
/* 135:    */   
/* 136:    */   private String getOutputFileName(String dirOut, String file)
/* 137:    */   {
/* 138:149 */     int idxStart = file.lastIndexOf(FILE_SEP);
/* 139:150 */     int idxEnd = file.indexOf('.', idxStart);
/* 140:153 */     if (idxEnd == -1) {
/* 141:154 */       idxEnd = file.length() - 1;
/* 142:    */     }
/* 143:157 */     String theOutFile = "";
/* 144:    */     
/* 145:    */ 
/* 146:160 */     String runOutputPath = dirOut + FILE_SEP + this.strRundId;
/* 147:161 */     File f = new File(runOutputPath);
/* 148:162 */     f.mkdirs();
/* 149:    */     
/* 150:    */ 
/* 151:165 */     theOutFile = runOutputPath + file.substring(idxStart, idxEnd) + "." + this.strRundId + ".system";
/* 152:    */     try
/* 153:    */     {
/* 154:170 */       PrintWriter writer = new PrintWriter(dirOut + FILE_SEP + "config." + this.strRundId + ".txt");
/* 155:171 */       this.properties.list(writer);
/* 156:172 */       writer.close();
/* 157:    */     }
/* 158:    */     catch (IOException exception)
/* 159:    */     {
/* 160:175 */       exception.printStackTrace();
/* 161:176 */       System.err.println("There seems to be some problem with the file names, please contact kganes2@illinois.edu");
/* 162:    */     }
/* 163:178 */     return theOutFile;
/* 164:    */   }
/* 165:    */   
/* 166:    */   public void doGenerateSummary(String fileName, String outfile, int taskId)
/* 167:    */   {
/* 168:184 */     SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> g = new SimpleDirectedWeightedGraph(DefaultWeightedEdge.class);
/* 169:185 */     OpinosisGraphBuilder builder = new OpinosisGraphBuilder();
/* 170:186 */     HashMap<String, Node> wordNodeMap = null;
/* 171:    */     try
/* 172:    */     {
/* 173:190 */       //BufferedReader reader = new BufferedReader(new FileReader(fileName));
File fileDir = new File(fileName);
BufferedReader reader = new BufferedReader( new InputStreamReader(new FileInputStream(fileDir), "UTF-8"));

/* 174:191 */       String str = "";
/* 175:192 */       int sentenceid = 0;
/* 176:194 */       while ((str = reader.readLine()) != null)
/* 177:    */       {
/* 178:195 */         sentenceid++;
/* 179:196 */         str = str.toLowerCase();
/* 180:197 */         wordNodeMap = builder.growGraph(str, 1, sentenceid);
/* 181:    */       }
/* 182:    */     }
/* 183:    */     catch (Exception exception)
/* 184:    */     {
/* 185:201 */       exception.printStackTrace();
/* 186:    */     }
/* 187:203 */     System.out.println("----------------TASK:" + taskId + "--------------------------");
/* 188:204 */     System.out.println("Generating Summaries for: " + fileName);
/* 189:205 */     System.out.println("Graph materialized...");
/* 190:206 */     Writer bla = new PrintWriter(System.out);
/* 191:207 */     g = builder.getGraph();
/* 192:    */     try
/* 193:    */     {
/* 194:213 */       System.out.println("Started summary generation...");
/* 195:214 */       BufferedWriter printer = FileUtil.getWriter(outfile);
/* 196:    */       
// We can change the summarizer
/* 197:216 */       OpinosisCore summarizer = new BasicSummarizer(g, wordNodeMap, printer);
/* 198:217 */       summarizer.start();
/* 199:218 */       System.out.println("Generated: " + outfile);
/* 200:219 */       System.gc();
/* 201:    */     }
/* 202:    */     catch (IOException exception)
/* 203:    */     {
/* 204:223 */       exception.printStackTrace();
/* 205:    */     }
/* 206:    */   }
/* 207:    */   
/* 208:    */   private int getReviewId(String str)
/* 209:    */   {
/* 210:259 */     int revid = -1;
/* 211:260 */     if (str.startsWith("#")) {
/* 212:261 */       revid = Integer.parseInt(str.substring(1, str.length()));
/* 213:    */     }
/* 214:264 */     return revid;
/* 215:    */   }
/* 216:    */ }

