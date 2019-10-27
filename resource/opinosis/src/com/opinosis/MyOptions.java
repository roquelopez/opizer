/*  1:   */ package com.opinosis;
/*  2:   */ 
/*  3:   */ import java.io.File;
/*  4:   */ import org.kohsuke.args4j.Option;
/*  5:   */ 
/*  6:   */ public class MyOptions
/*  7:   */ {
/*  8:   */   @Option(name="-b", usage="Base directory where input and output directories are found.\nThis directory should contain the following subdirectories:\n\tinput/  - All the text to be summarized. One file per document.\n\toutput/ - Summarization Results (opinosis summaries)\n\tetc/  - Other resources like Opinosis.properties will be stored here.")
/*  9:13 */   private File dirBase = null;
/* 10:   */   
/* 11:   */   public File getDirBase()
/* 12:   */   {
/* 13:16 */     return this.dirBase;
/* 14:   */   }
/* 15:   */   
/* 16:   */   public void setDirBase(File dirBase)
/* 17:   */   {
/* 18:20 */     this.dirBase = dirBase;
/* 19:   */   }
/* 20:   */ }


