// CA - voix totale
// Cellular Automata
// MIDI generator for monophonic synthesizer
//
// Nguyen Minh Duc
// www.nguyenminhduc.de
// ------------------------------------------------------------------------------
// ------------------------------------------------------------------------------

import java.util.*;
import themidibus.*;
import controlP5.*;
import spout.*;

Spout spout;
ControlFrame cf;
MidiBus myBus; 

boolean shareSpout = false;

boolean update = false;

boolean stopping = false;
boolean playMIDI = false;

boolean useScale = false;

int mode=0;
int nmode=0;
int mmode=0;
int cmode=0;
int c2mode = 0;
int smode = 0;

int newmode=0;
int newnmode=0;
int newmmode=0;
int newcmode=0;
int newc2mode=0;
int newsmode=0;


boolean newvalues = false;

float newmapbegin=0;
float newmapend=1;
float newsumbegin=0;
float newsumend=1;

float mapbegin = 0;
float mapend = 1;
float sumbegin = 0;
float sumend = 1;

boolean noRepeat = true;

int count = 0;

int maxsum;
int maxstates = 2;
int size = 20;
int depth = 5;
int rows;
int columns;
int delaytime = 250;
int delayinnertime = 50;

Checkerboard board;

int MIDIchannel = 0;
int MIDIdevice = 0;
boolean startMIDI = false;

int selectX = 0;
int selectY = 0;
int curve = 0;

int newnote = 0;
int val = 0;

IntList newscale = new IntList();
IntList scale = new IntList();
int newscalesize = 127;
int scalesize = 127;

void settings() {
  size(641, 641, P2D);
}

void setup() {

  spout = new Spout(this);

  cf = new ControlFrame(this, 800, 1000, "Controls");
  surface.setLocation(820, 10);

  for (int i=0; i <128; i++) {
    newscale.append(i);
    scale.append(i);
  }

  background(0);
}

void evolve() {
  switch (mode) {
  case 0:
    board.evolveGOL();
    break;
  case 1:
    board.evolveXOR(nmode);
    break;
  case 2:
    board.evolveDCS();
    break;
  }

  board.display();
  count++;
}

void draw() {

  if (update) {
    update = false;
    board.display();
  }

  if (newvalues == true) {

    myBus.sendNoteOff(MIDIchannel, scale.get(val), 127);

    scale.clear();
    scale.append(newscale);

    scalesize = newscalesize;
    sumbegin = newsumbegin;
    sumend = newsumend;
    mapbegin = newmapbegin;
    mapend = newmapend;

    mode = newmode;
    nmode = newnmode;
    mmode = newmmode;
    cmode = newcmode;
    c2mode = newc2mode;
    smode = newsmode;

    newvalues = false;
  }


  if (stopping) {
    if (!playMIDI) {
      evolve();
      delay(delaytime);
    } else {


      if (mmode == 0) {

        evolve();

        int all = 0;

        if (mode!=2)
          all = board.getLiving();
        else if (mode ==2)
          all = board.getSum();

        if (cmode == 0) {

          int min = floor(maxsum*sumbegin);
          if (min > all) min = all;
          int max = ceil(maxsum*sumend);
          if (max < all) max = all;
          int smin = floor(scalesize*mapbegin);
          int smax = ceil(scalesize*mapend);

          newnote = int(map(all, min, max, smin, smax));
        } else if (cmode == 1)
          newnote = all % scalesize;


        if ((val != newnote)||noRepeat == false) {
          if (val<scalesize)
            myBus.sendNoteOff(MIDIchannel, scale.get(val), 127);
          delay(delayinnertime);
          val = newnote;
          myBus.sendNoteOn(MIDIchannel, scale.get(val), 127); 
          print(midi(scale.get(val)) + " | ");
        } else {        
          print(" %%% | ");
        }
      }

      if (mmode == 1) {
        if (!startMIDI) {
          evolve();
          startMIDI = true;
        } else {
          if (board.getCell(selectX, selectY).getState()==1) {

            if (c2mode == 0) {
              int xy = selectX * selectY;

              int minxy = floor(rows*columns*sumbegin);
              if (minxy > xy) minxy = xy;
              int maxxy = ceil(rows*columns*sumend); 
              if (maxxy < xy) maxxy = xy;

              int smin = floor(scalesize*mapbegin);
              int smax = ceil(scalesize*mapend);

              newnote = int(map(xy, minxy, maxxy-2, smin, smax));
            } else if (c2mode == 1) {

              newnote = 0;

              boolean b0 = boolean(board.getCornerCodeOf(selectX, selectY)[0]);
              boolean b1 = boolean(board.getCornerCodeOf(selectX, selectY)[1]);
              boolean b2 = boolean(board.getCornerCodeOf(selectX, selectY)[2]);
              boolean b3 = boolean(board.getCornerCodeOf(selectX, selectY)[3]);         

              boolean b4 = boolean(board.getEdgeCodeOf(selectX, selectY)[0]);
              boolean b5 = boolean(board.getEdgeCodeOf(selectX, selectY)[1]);
              boolean b6 = boolean(board.getEdgeCodeOf(selectX, selectY)[2]);
              boolean b7 = boolean(board.getEdgeCodeOf(selectX, selectY)[3]);

              boolean[] w = new boolean[]{(b0 && b4), (b1 && b5), (b2 && b6), (b3 && b7)};
              boolean[] v = new boolean[]{(b0 || b4), (b1 || b5), (b2 || b6), (b3 || b7)};
              boolean[] x = new boolean[]{(b0 ^ b4), (b1 ^ b5), (b2 ^ b6), (b3 ^ b7)};


              for (int b = 4; b < 7; b++)
                if (w[b-4])
                  newnote += pow(2, b);
              for (int b = 0; b < 3; b++)
                if (v[b])
                  newnote += pow(2, b);  

              if (newnote == 0) {
                newnote = -1;
              } else {

                int smin = floor(scalesize*mapbegin);
                int smax = ceil(scalesize*mapend);
                newnote = int(map(newnote, 0, 255, smin, smax));
              }
            }
            if (newnote != -1) {
              if ((val != newnote)||(noRepeat == false)) {

                if (val<scalesize)
                  myBus.sendNoteOff(MIDIchannel, scale.get(val), 127);
                delay(delayinnertime);
                val = newnote;
                myBus.sendNoteOn(MIDIchannel, scale.get(val), 127); 
                board.getCell(selectX, selectY).displayColor(255, 0, 0);
                print(midi(scale.get(val)) + " | ");
              } else {        
                print(" %%% | ");     
                board.getCell(selectX, selectY).displayColor(0, 0, 255);
              }
            } else {
              print(" &&& | ");     
              board.getCell(selectX, selectY).displayColor(0, 255, 255);

              if (val<scalesize)
                myBus.sendNoteOff(MIDIchannel, scale.get(val), 127);
            }
          }


          if (smode == 1) {

            do {
              selectY++;
              if (selectY > rows - 1 ) {
                selectY = 0;
                selectX++;
              }
              if (selectX > columns - 1) {
                selectX = 0;
                startMIDI = false;
                break;
              }
            } while (board.getCell(selectX, selectY).getState()!=1);
          } else if (smode == 0) {
            do {
              selectX++;
              if (selectX > columns - 1 ) {
                selectX = 0;
                selectY++;
              }
              if (selectY > rows - 1) {
                selectY = 0;
                startMIDI = false;
                break;
              }
            } while (board.getCell(selectX, selectY).getState()!=1);
          } else if (smode == 2) {
            do {
              curve++;
              selectX = int(t2xyI(curve, rows*columns).x);
              selectY = int(t2xyI(curve, rows*columns).y);     

              if (curve==rows*columns) {
                selectX = 0;
                selectY = 0;
                curve = 0;
                startMIDI = false;  
                break;
              }
            } while (board.getCell(selectX, selectY).getState()!=1);
          }
        }
      }

      delay(delaytime);
    }
  }

  if (shareSpout)
    spout.sendTexture();
}




// ---------------------------------------------------------------------------
// -------------------------------MIDI----------------------------------------
// ---------------------------------------------------------------------------
String midi(int val) {

  int oct = floor(val / 12);
  String n = "";

  switch(val % 12) {
  case 0:
    n = "C";
    break;
  case 1:
    n = "C#";
    break;
  case 2:
    n = "D";
    break;
  case 3:
    n = "D#";
    break;
  case 4:
    n = "E";
    break;
  case 5:
    n = "F";
    break;
  case 6:
    n = "F#";
    break;
  case 7:
    n = "G";
    break;
  case 8:
    n = "G#";
    break;
  case 9:
    n = "A";
    break;
  case 10:
    n = "A#";
    break;
  case 11:
    n = "B";
    break;
  }

  return oct+n;
}

//******************************************************************
//*************HILBERT**********************************************
// Drehspiegelung eines Quadrates
PVector rot(int x, int y, int rx, int ry, int p) {
  if (ry == 0) {
    if (rx == 1) {
      x = p-1 - x;
      y = p-1 - y;
    }
    // vertausche x und y
    int z = x;
    x = y;
    y = z;
  }
  return new PVector(x, y);
}

PVector t2xyI(int t, int p) {
  PVector xy = new PVector(0, 0); // im Ergebnisquadrat die linke untere Ecke
  for (int m = 1; m < p; m *= 2) { // m wächst exponentiell
    int rx = 1 & t/2;      // Binärziffer[1]: 0=links/1=rechts
    int ry = 1 & (t ^ rx); // Binärziffer[0]
    xy = rot(int(xy.x), int(xy.y), rx, ry, m);
    xy.x += m * rx;
    xy.y += m * ry;
    t /= 4; // zur nächsten Quaternärziffer
  }
  return xy;
}



// ---------------------------------------------------------------------------
// ------------optional manipulation with Mouse and Keyboard------------------
// ---------------------------------------------------------------------------
void mouseClicked() {
  if (board != null) {
    int x = floor(mouseX/size);
    int y = floor(mouseY/size);
    int state = board.getCell(x, y).getState();
    if (state==0)
      state = 1;
    else
      state = 0;
    board.getCell(x, y).initState(state);
    board.getCell(x, y).display();
  }
}
