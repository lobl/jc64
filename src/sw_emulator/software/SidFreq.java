/*
 * @(#)SidFreq.java 2019/12/30
 *
 * ICE Team free software group
 *
 * This file is part of JIIT64 Java Ice Team Tracker 64
 * See README for copyright notice.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 */
package sw_emulator.software;

import sw_emulator.math.Unsigned;

/**
 * Modified version of SidFreq by XSidPlay2
 * 
 * @author ice
 */
public class SidFreq {        
  /** A4 index in table */  
  static final int A4=57;  
  
  /** Table size */
  static final int TABLE = 96-6;
  
  /** All table size */
  static final int ALL = 96;
  
  /** Short table size */
  static final int SHORT=72;
  
  /** Short2 table size */
  static final int SHORT2=65;  
  
  /** Error for note difference */
  static final int ERROR=6;
    
  /** Buffer of data */  
  byte[] inB;

  /** Memory flags */
  MemoryDasm[] memory;  
  
  /** Start in buffer */
  int start;
  
  /** End in buffer */
  int end;
  
  /** Offset to add for having memory address */
  int offset;
  
  /** Label for low frequency */
  String lowLabel;
  
  /** Label for high frequency */
  String highLabel;
  
  /** Instance of the class as singleton */
  public static final SidFreq instance=new SidFreq();
  
  /**
   * Private constructor
   */
  private SidFreq() {      
  }
  
/**
 * Identify the frequency of the music 
 * 
 * @param inB the memory buffer
 * @param memory the memory dasm
 * @param start start address in buffer
 * @param end end address in buffer
 * @param offset offset to add for having memory address
 * @param lowLabel label for low frequency
 * @param highLabel label for high frequency
 */  
  public void identifyFreq(byte[] inB, MemoryDasm[] memory, int start, int end, int offset, String lowLabel, String highLabel) {
    this.inB=inB;  
    this.memory=memory;
    this.start=start;
    this.end=end;
    this.offset= offset;
    this.lowLabel=lowLabel;
    this.highLabel=highLabel;
    
    if (linearTable()) return;
    if (combinedTable()) return;
    if (shortLinearTable()) return;        
    if (shortCombinedTable()) return;
    if (linearInverseTable()) return;
    highOctave();
  }
  
  /**
   * Search for tables in linear way (low / high or high / low)
   *  
   * @return true if the table is fount
   */
  private boolean linearTable() {
    int i;  
    int sid;
    int high=-1;
    int low=-1;   
    
    // check for high frequency table
    for (i=start; i<end-TABLE; i++) {
      if (searchHigh(i)) {
        high=i;
        break;         
      }
    }  
    
    // look if high table was fount
    if (high==-1) return false;      
    
    // check for low frequency table (first part)
    if (high>TABLE) {
      for (i=0; i<=high-TABLE; i++) {
        if (searchLow(high, i)) {
          low=i;
          break;
        }           
      }
    
      // check for high frequency table (second part)
      if ((low==-1) && (high<end-TABLE*2)) {
        for (i=high+TABLE; i<end-TABLE; i++) {
          if (searchLow(high, i)) {
            low=i;
            break;
          }                 
        }
      }
    
      // look if low table was fount
      if (low==-1) return false;
    }
    
    // get the A4 note
    sid=Unsigned.done(inB[high+A4])*256+Unsigned.done(inB[low+A4]);  

    addData(high, low, sid);
    markMemory(high, high+TABLE+6, 1);
    markMemory(low, low+TABLE+6, 1);
    
    return true;    
  }

  /**
   * Search for tables in combined way (low + high or high + low)
   * 
   * @return true if the table is fount
   */
  private boolean combinedTable() {
    int sid;
    int i;
    int high=-1;
    int low=-1;
     
    // check for high frequency table
    for (i=0; i<end-TABLE*2; i++) {
      if (searchHigh2(i)) {
        high=i;
        break;         
      }
    }  
       
    // look if high table was fount
    if (high==-1) return false;  
  
    // check for low frequency table
    for (i=0; i<end-TABLE*2; i++) {
      if (i!=high) {
        if (searchLow2(high, i)) {
          low=i;
          break;         
        }
      }
    }
   
    // look if low table was fount
    if (low==-1) return false;  
  
    // get the A4 note
    sid=Unsigned.done(inB[high+A4*2])*256+Unsigned.done(inB[low+A4*2]);  
    
    addData(high, low, sid);
    markMemory(high, high+TABLE+6, 2);
    markMemory(low, low+TABLE+6, 2);
      
    return true;    
  }
  
  /**
   * Search for short tables in combined way (low + high or high + low)
   * 
   * @return true if the table is fount
   */
  private boolean shortCombinedTable() {
    int sid;
    int i;
    int high=-1;
    int low=-1;
     
    // check for high frequency table
    for (i=0; i<end-SHORT2*2; i++) {
      if (searchShortHigh2(i)) {
        high=i;
        break;         
      }
    }  
       
    // look if high table was fount
    if (high==-1) return false;  
  
    // check for low frequency table
    for (i=0; i<end-SHORT2*2; i++) {
      if (i!=high) {
        if (searchShortLow2(high, i)) {
          low=i;
          break;         
        }
      }
    }
   
    // look if low table was fount
    if (low==-1) return false;  
  
    // get the A4 note
    sid=Unsigned.done(inB[high+A4*2])*256+Unsigned.done(inB[low+A4*2]);  
    
    addData(high, low, sid);
    markMemory(high, high+SHORT2*2, 2);
    markMemory(low, low+SHORT2*2, 2);
      
    return true;    
  }

  /**
   * Search for short tables in linear way (low / high or high / low)
   * 
   * @return true if the table is fount
   */
  private boolean shortLinearTable() {
    int sid;
    int i;
    int high=-1;
    int low=-1;
     
    // check for high frequency table
    for (i=0; i<end-SHORT; i++) {
      if (searchShortHigh(i)) {
        high=i;
        break;         
      }
    }
       
    // look if high table was fount
    if (high==-1) return false;
     
    // check for low frequency table (first part)
    if (high>SHORT) {
      for (i=0; i<=high-SHORT; i++) {
        if (searchShortLow(high, i)) {
          low=i;
          break;
        }           
      }
    }
     
    // check for high frequency table (second part)
    if ((low==-1) && (high<end-SHORT*2)) {
      for (i=high+SHORT; i<end-SHORT; i++) {
        if (searchShortLow(high, i)) {
          low=i;
          break;
        }                   
      }
    }
      
    // look if low table was fount
    if (low==-1) return false;
     
    // get the A4 note
    sid=Unsigned.done(inB[high+A4])*256+Unsigned.done(inB[low+A4]);   
   
    addData(high, low, sid);
    markMemory(high, high+SHORT+6, 1);
    markMemory(low, low+SHORT+6, 1);
      
    return true;    
   }
  
  /**
   * Search for the high frequency table in linear way
   *
   * @param index the index where to start the test
   * @return true if there is a high frequency table in that position 
   */ 
  private boolean searchHigh(int index) {
    int i;
    int actual=1;   
   
    // it must start with three 1
    if ( ((int)inB[index+0]!=1) || ((int)inB[index+1]!=1) || ((int)inB[index+2]!=1)) return false;
   
    // search for increasing numbers
    for (i=index+3; i<index+TABLE; i++) {
      if ((int)(inB[i]& 0xFF)<actual) {
        // catch a very big error on Vibrants/JO note table at 424Hz 
        if (actual==7 && (int)(inB[i]& 0xFF)==3 && (int)(inB[i-2]& 0xFF)==3)  actual=((int)inB[i]& 0xFF); 
        else return false;
      }   
      else actual=((int)inB[i]& 0xFF);
    }
   
    return true;   
  }  
  
   /**
   * Search for the short high frequency table in linear way
   *
   * @param index the index where to start the test
   * @return true if there is a high frequency table in that position 
   */ 
  private boolean searchShortHigh(int index) {
    int i;
    int actual=1;   
   
    // it must start with three 1
    if ( ((int)(inB[index+0]& 0xFF)!=1) || ((int)(inB[index+1]& 0xFF)!=1) || ((int)(inB[index+2]& 0xFF)!=1)) return false;
   
    // search for increasing numbers
    for (i=index+3; i<index+SHORT; i++) {
      if ((int)(inB[i]& 0xFF)<actual) return false;
      else actual=(int)(inB[i]& 0xFF);
    }
   
    return true;   
  } 
  
  /**
   * Search for the high frequency table in *2 way
   *
   * @param index the index where to start the test
   * @return true if there is a high frequency table in that position 
   */ 
  private boolean searchHigh2(int index) {
    int i;
    int actual=1;   
   
    // it must start with three 1
    if ( ((int)(inB[index+0]& 0xFF)!=1) || ((int)(inB[index+2]& 0xFF)!=1) || ((int)(inB[index+4]& 0xFF)!=1)) return false;
   
    // search for increasing numbers
    for (i=index+6; i<index+TABLE*2; i++, i++) {
      if ((int)(inB[i]& 0xFF)<actual) return false;
      else actual=(int)(inB[i]& 0xFF);
    }
   
    return true;   
  } 
  
  /**
   * Search for the high frequency table in *2 way
   *
   * @param index the index where to start the test
   * @return true if there is a high frequency table in that position 
   */ 
  private boolean searchShortHigh2(int index) {
    int i;
    int actual=1;   
   
    // it must start with three 1
    if ( ((int)(inB[index+0]& 0xFF)!=1) || ((int)(inB[index+2]& 0xFF)!=1) || ((int)(inB[index+4]& 0xFF)!=1)) return false;
   
    // search for increasing numbers
    for (i=index+6; i<index+SHORT2*2; i++, i++) {
      if ((int)(inB[i]& 0xFF)<actual) return false;
      else actual=(int)(inB[i]& 0xFF);
    }
   
    return true;   
  } 
  
  /**
   * Search for a low frequency table in linear way
   * 
   * @param high the position of high table
   * @param index the index where to search for low table
   * @return true if there is a low frequency table in that position 
   */ 
  private boolean searchLow(int high, int index){
    int i;
    int note1, note2, note3, note4, note5, note6, note7;
    int diff;
   
    boolean errSoundTracker=false;
    boolean errGrooPsygon=false;
    boolean errVibrantsJO=false;
    
    // scan all notes
    for (i=0; i<12; i++) {
      note1=(int)(inB[high+i]& 0xFF)*256+(int)(inB[index+i]& 0xFF);
      note2=(int)(inB[high+i+12]& 0xFF)*256+(int)(inB[index+i+12]& 0xFF);
      note3=(int)(inB[high+i+12*2]& 0xFF)*256+(int)(inB[index+i+12*2]& 0xFF);
      note4=(int)(inB[high+i+12*3]& 0xFF)*256+(int)(inB[index+i+12*3]& 0xFF);
      note5=(int)(inB[high+i+12*4]& 0xFF)*256+(int)(inB[index+i+12*4]& 0xFF);
      note6=(int)(inB[high+i+12*5]& 0xFF)*256+(int)(inB[index+i+12*5]& 0xFF);
      note7=(int)(inB[high+i+12*6]& 0xFF)*256+(int)(inB[index+i+12*6]& 0xFF);
  
      diff=0;
      diff+=Math.abs(note1*2 - note2);
      if (i<11) diff+=Math.abs(note2*2 - note3);
      if (i<11) diff+=Math.abs(note3*2 - note4);
      if (i<11) diff+=Math.abs(note4*2 - note5);
      if (i<11) diff+=Math.abs(note5*2 - note6);
      if (i<11) diff+=Math.abs(note6*2 - note7);
      
      // catch error into Vibrants/JO note table at 416Hz  
      if (i==0 && diff==212) continue;   
      
         
      // catch an error on MUSICIANS/P/PseudoGrafx/Fonttime.sid (short table seems 476Hz)
      if (i==2 && diff==25) continue;    
      
      // catch 3 errors into Sound Tracher 64 at 459Hz
      if (i==2 && diff==48) { errSoundTracker=true; continue;}  
      if (errSoundTracker) {
        if (i==9 && diff==302) continue; 
        if (i==10 && diff==514) continue;
      }  
    
      // catch an error onto TFX note table 449Hz
      if (i==3 && diff==25) continue; 

      // catch 4 errors into Vibrants/JO note table at 416Hz 
      if (i==3 && diff==118) {errVibrantsJO=true; continue;} 
      if (errVibrantsJO) {
         if (i==4 && diff==153) continue;
         if (i==5 && diff==193) continue;
         if (i==7 && diff==110) continue;
      }   
            
      // catch 3 errors onto Groo/Psygon at 434Hz
      if (i==3 && diff==48) {errGrooPsygon=true; continue;}
      if (errGrooPsygon) {
        if (i==10 && diff==302) continue; 
        if (i==11 && diff==512) continue;
      }     
    
      // catch an error onto TFX 2.7 note table (table 424Hz: E0 is 0147 instead of 0151)
      // catch an error onto TFX 2.9 note table (table 449Hz: D#0 is 0147 instead of 0151)
      // catch an error into DMC note table (table 424Hz: E0 is 0147 instead of 0151)
      if (i==4 && diff==25) continue;
      
      // catch 1 error into Vibrants/JO note table at 440Hz
      if (i==4 && diff==127) continue;

      // catch a big error on Music Mixer notes table (table 440Hz: F#4 - values 18E0 instead of 189C)
      if (i==6 && diff==206) continue;
      
      // catch a very big error (originated in high table) on Vibrants/JO note table at 424Hz 
      if (i==8 && diff==3074) continue;
    
      // catch ha error on DEMOS/M-R/Max_Mix.sid (table 440Hz; A2 - values 0744 instead of 0751)
      if (i==9 && diff==42) continue;
      
      // catch errors onto Mon/Futurecomposer (table: 424Hz; B1 - values 03E0 instead of 03F4)
      if (i==11 && diff==20) continue;
      
      if (diff>ERROR) return false;
    }
   
    return true;
  } 
  
  /**
    * Search for a low frequency short table in linear way
    * 
    * @param high the position of high table
    * @param index the index where to search for low table
    * @return true if there is a low frequency table in that position 
   */ 
  private boolean searchShortLow(int high, int index){
    int i;
    int note1, note2, note3, note4, note5, note6, note7;
    int diff;
   
    // scan all notes
    for (i=0; i<12; i++) {
      note1=(int)(inB[high+i]& 0xFF)*256+(int)(inB[index+i]& 0xFF);
      note2=(int)(inB[high+i+12]& 0xFF)*256+(int)(inB[index+i+12]& 0xFF);
      note3=(int)(inB[high+i+12*2]& 0xFF)*256+(int)(inB[index+i+12*2]& 0xFF);
      note4=(int)(inB[high+i+12*3]& 0xFF)*256+(int)(inB[index+i+12*3]& 0xFF);
      note5=(int)(inB[high+i+12*4]& 0xFF)*256+(int)(inB[index+i+12*4]& 0xFF);
  
      diff=0;
      diff+=Math.abs(note1*2 - note2);
      diff+=Math.abs(note2*2 - note3);
      diff+=Math.abs(note3*2 - note4);
      diff+=Math.abs(note4*2 - note5);
         

      // catch errors onto Martin Galway player (table: 442Hz)
      if (i==3 && diff==31) continue;     // table 434Hz
      if (i==8 && diff==87) continue;
      if (i==10 && diff==33) continue;
      
      // catch errors in Master Composer     
      if (i==2 && diff==31) continue;  
      
      // catch an error into DMC note table
      if (i==4 && diff==24) continue;    
      if (i==5 && diff==204) continue;  

    
      if (diff>ERROR) return false;
    }
   
    return true;
  }  
  
  /**
   * Search for a low frequency table in *2 way
   * 
   * @param high the position of high table
   * @param index the index where to search for low table
   * @return true if there is a low frequency table in that position 
   */ 
  private boolean searchLow2(int high, int index) {
    int i;
    int note1, note2, note3, note4, note5, note6, note7;
    int diff;
    
    boolean errVibrantsJO=false;
   
    // scan all notes
    for (i=0; i<12; i++) {
      note1=(int)(inB[high+i*2]& 0xFF)*256+(int)(inB[index+i*2]& 0xFF);
      note2=(int)(inB[high+i*2+24]& 0xFF)*256+(int)(inB[index+i*2+24]& 0xFF);
      note3=(int)(inB[high+i*2+24*2]& 0xFF)*256+(int)(inB[index+i*2+24*2]& 0xFF);
      note4=(int)(inB[high+i*2+24*3]& 0xFF)*256+(int)(inB[index+i*2+24*3]& 0xFF);
      note5=(int)(inB[high+i*2+24*4]& 0xFF)*256+(int)(inB[index+i*2+24*4]& 0xFF);
      note6=(int)(inB[high+i*2+24*5]& 0xFF)*256+(int)(inB[index+i*2+24*5]& 0xFF);
      note7=(int)(inB[high+i*2+24*6]& 0xFF)*256+(int)(inB[index+i*2+24*6]& 0xFF);
      
      diff=0;
      diff+=Math.abs(note1*2 - note2);
      diff+=Math.abs(note2*2 - note3);
      diff+=Math.abs(note3*2 - note4);
      if (i<11) diff+=Math.abs(note4*2 - note5);
      if (i<11) diff+=Math.abs(note5*2 - note6);
      if (i<11) diff+=Math.abs(note6*2 - note7);
      
      // catch 4 errors into Vibrants/JO note table at 434Hz 
      if (i==3 && diff==48) { errVibrantsJO=true; continue; }
      if (errVibrantsJO) {
        if (i==8 && diff==81) continue; 
        if (i==9 && diff==264) continue;   
        if (i==10 && diff==302) continue; 
      }   
      
      if (diff>ERROR) return false;
    }
   
    return true;    
  }  
  
  /**
   * Search for a low frequency table in *2 way
   * 
   * @param high the position of high table
   * @param index the index where to search for low table
   * @return true if there is a low frequency table in that position 
   */ 
  private boolean searchShortLow2(int high, int index) {
    int i;
    int note1, note2, note3, note4, note5, note6, note7;
    int diff;
       
    // scan all notes
    for (i=0; i<12; i++) {
      note1=(int)(inB[high+i*2]& 0xFF)*256+(int)(inB[index+i*2]& 0xFF);
      note2=(int)(inB[high+i*2+24]& 0xFF)*256+(int)(inB[index+i*2+24]& 0xFF);
      note3=(int)(inB[high+i*2+24*2]& 0xFF)*256+(int)(inB[index+i*2+24*2]& 0xFF);
      note4=(int)(inB[high+i*2+24*3]& 0xFF)*256+(int)(inB[index+i*2+24*3]& 0xFF);
      note5=(int)(inB[high+i*2+24*4]& 0xFF)*256+(int)(inB[index+i*2+24*4]& 0xFF);
      
      diff=0;
      diff+=Math.abs(note1*2 - note2);
      diff+=Math.abs(note2*2 - note3);
      diff+=Math.abs(note3*2 - note4);
      diff+=Math.abs(note4*2 - note5);

      
      // catch an error into Vibrants/JO note table at 434Hz
      if (i==3 && diff==31) continue; 
      
      // catch an error into Vibrants/JO note table at 434Hz
      if (i==4 && diff==363) continue; 
      
      if (diff>ERROR) return false;
    }
   
    return true;    
  }  
  
  /**
   * Add the collected data to memory dasm
   * 
   * @param high the high position
   * @param low the low position
   * @param sid the sid A4 word freq
   */
  private void addData(int high, int low, int sid) {
    memory[high+offset].userLocation=highLabel;
    memory[low+offset].userLocation=lowLabel;
    
    int freqNTSC=(int)Math.round(sid*0.0609592795372);
    int freqPAL=(int)Math.round(sid*0.0587253570557);    
    
    memory[low+offset].userComment="A4="+freqPAL+" HZ (PAL) | A4="+freqNTSC+" HZ (NTSC)";
    memory[high+offset].userComment="A4="+freqPAL+" HZ (PAL) | A4="+freqNTSC+" HZ (NTSC)";
  }
  
  /**
   * Mark the memory as data
   * 
   * @param start the position to start
   * @param end the end position
   * @param step the step to use
   */
  private void markMemory(int start, int end, int step) {
    for (int i=start; i<end; i+=step) {
      if (!memory[i+offset].isCode && !memory[i+offset].isData) memory[i+offset].isData=true;
    }   
  }

  /**
   * Looks for an high octave table
   * 
   * @return true if it is find
   */
  private boolean highOctave() {
    final double STEP=Math.pow(2, 1/12);
    int[] freq=new int[12];
      
    for (int i=0; i<end-13*2; i++) {
      if ((int)inB[i]==0 && (int)inB[i+13]==0) {
        freq[0]=(int)(inB[i+1]& 0xFF)+(int)(inB[i+14]& 0xFF)*256; 
        if (freq[0]==0) continue;
        freq[1]=(int)(inB[i+2]& 0xFF)+(int)(inB[i+15]& 0xFF)*256;
        if (freq[1]==0 || freq[1]<=freq[0] || Math.abs(freq[1]/freq[0]-STEP)>0.00001) continue;        
        freq[2]=(int)(inB[i+3]& 0xFF)+(int)(inB[i+16]& 0xFF)*256;
        if (freq[2]==0 || freq[2]<=freq[1] || Math.abs(freq[2]/freq[1]-STEP)>0.00001) continue;
        freq[3]=(int)(inB[i+4]& 0xFF)+(int)(inB[i+17]& 0xFF)*256;
        if (freq[3]==0 || freq[3]<=freq[2] || Math.abs(freq[3]/freq[2]-STEP)>0.00001) continue;
        freq[4]=(int)(inB[i+5]& 0xFF)+(int)(inB[i+18]& 0xFF)*256;
        if (freq[4]==0 || freq[4]<=freq[3] || Math.abs(freq[4]/freq[3]-STEP)>0.00001) continue;
        freq[5]=(int)(inB[i+6]& 0xFF)+(int)(inB[i+19]& 0xFF)*256;        
        if (freq[5]==0 || freq[5]<=freq[4] || Math.abs(freq[5]/freq[4]-STEP)>0.00001) continue;
        freq[6]=(int)(inB[i+7]& 0xFF)+(int)(inB[i+20]& 0xFF)*256;
        if (freq[6]==0 || freq[6]<=freq[5] || Math.abs(freq[6]/freq[5]-STEP)>0.00001) continue;
        freq[7]=(int)(inB[i+8]& 0xFF)+(int)(inB[i+21]& 0xFF)*256;
        if (freq[7]==0 || freq[7]<=freq[6] || Math.abs(freq[7]/freq[6]-STEP)>0.00001) continue;
        freq[8]=(int)(inB[i+9]& 0xFF)+(int)(inB[i+22]& 0xFF)*256;
        if (freq[8]==0 || freq[8]<=freq[7] || Math.abs(freq[8]/freq[7]-STEP)>0.00001) continue;
        freq[9]=(int)(inB[i+10]& 0xFF)+(int)(inB[i+23]& 0xFF)*256;
        if (freq[9]==0 || freq[9]<=freq[8] || Math.abs(freq[9]/freq[8]-STEP)>0.00001) continue;
        freq[10]=(int)(inB[i+11]& 0xFF)+(int)(inB[i+24]& 0xFF)*256;
        if (freq[10]==0 || freq[10]<=freq[9] || Math.abs(freq[10]/freq[9]-STEP)>0.00001) continue;
        freq[11]=(int)(inB[i+12]& 0xFF)+(int)(inB[i+25]& 0xFF)*256;
        if (freq[11]==0 || freq[11]<=freq[10] || Math.abs(freq[11]/freq[10]-STEP)>0.00001) continue;
        
        if (freq[11]<62000) continue;

        addData(i+13, i, freq[9]/8);
        markMemory(i+13, i+26, 1);
        markMemory(i, i+13, 1);
      }  
    }      
    
    return false;      
  }    
    
  /**
   * Search for tables in linear way (low / high or high / low) with inverse direction
   *  
   * @return true if the table is fount
   */
  private boolean linearInverseTable() {
    int i;  
    int sid;
    int high=-1;
    int low=-1;   
    
    // check for high frequency table
    for (i=end-1; i>=start+ALL; i--) {
      if (searchInverseHigh(i)) {
        high=i;
        break;         
      }
    }  
    
    // look if high table was fount
    if (high==-1) return false;      

    // check for low frequency table (first part)
    if (high>ALL) {
      for (i=0+ALL; i<high; i++) {
        if (searchInverseLow(high, i)) {
          low=i;
          break;
        }           
      }
    
      // check for high frequency table (second part)
      if ((low==-1) && (high<end-ALL)) {
        for (i=high+ALL; i<end; i++) {
          if (searchInverseLow(high, i)) {
            low=i;
            break;
          }                 
        }
      }
    
      // look if low table was fount
      if (low==-1) return false;
    }
    
    // get the A4 note
    sid=Unsigned.done(inB[high-A4])*256+Unsigned.done(inB[low-A4]);  

    addData(high-ALL+1, low-ALL+1, sid);
    markMemory(high-ALL+1, high, 1);
    markMemory(low-ALL+1, low, 1);
 
    return true;    
  }
   
/**
   * Search for the high frequency inverse table in linear way
   *
   * @param index the index where to start the test
   * @return true if there is a high frequency table in that position 
   */ 
  private boolean searchInverseHigh(int index) {
    int i;
    int actual=1;   
   
    // it must start with three 1
    if ( ((int)inB[index-0]!=1) || ((int)inB[index-1]!=1) || ((int)inB[index-2]!=1)) return false;
   
    // search for increasing numbers
    for (i=index-3; i>index-ALL; i--) {
      if ((int)(inB[i]& 0xFF)<actual) return false;
      else actual=((int)inB[i]& 0xFF);
    }

    return true;   
  }    
  
/**
   * Search for a low frequency inverse table in linear way
   * 
   * @param high the position of high table
   * @param index the index where to search for low table
   * @return true if there is a low frequency table in that position 
   */ 
  private boolean searchInverseLow(int high, int index){
    int i;
    int note1, note2, note3, note4, note5, note6, note7;
    int diff;
       
    // scan all notes
    for (i=0; i<12; i++) {
      note1=(int)(inB[high-i]& 0xFF)*256+(int)(inB[index-i]& 0xFF);
      note2=(int)(inB[high-i-12]& 0xFF)*256+(int)(inB[index-i-12]& 0xFF);
      note3=(int)(inB[high-i-12*2]& 0xFF)*256+(int)(inB[index-i-12*2]& 0xFF);
      note4=(int)(inB[high-i-12*3]& 0xFF)*256+(int)(inB[index-i-12*3]& 0xFF);
      note5=(int)(inB[high-i-12*4]& 0xFF)*256+(int)(inB[index-i-12*4]& 0xFF);
      note6=(int)(inB[high-i-12*5]& 0xFF)*256+(int)(inB[index-i-12*5]& 0xFF);
      note7=(int)(inB[high-i-12*6]& 0xFF)*256+(int)(inB[index-i-12*6]& 0xFF);
  
      diff=0;
      diff+=Math.abs(note1*2 - note2);
      if (i<11) diff+=Math.abs(note2*2 - note3);
      if (i<11) diff+=Math.abs(note3*2 - note4);
      if (i<11) diff+=Math.abs(note4*2 - note5);
      if (i<11) diff+=Math.abs(note5*2 - note6);
      if (i<11) diff+=Math.abs(note6*2 - note7);
            
      if (diff>ERROR) return false;
    }
   
    return true;
  }   

}
