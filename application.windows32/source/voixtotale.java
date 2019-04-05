import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.*; 
import themidibus.*; 
import controlP5.*; 
import spout.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class voixtotale extends PApplet {

// CA - voix totale
// Cellular Automata
// MIDI generator for monophonic synthesizer
//
// Nguyen Minh Duc
// www.nguyenminhduc.de
// ------------------------------------------------------------------------------
// ------------------------------------------------------------------------------






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

public void settings() {
  size(641, 641, P2D);
}

public void setup() {

  spout = new Spout(this);

  cf = new ControlFrame(this, 800, 1000, "Controls");
  surface.setLocation(820, 10);

  for (int i=0; i <128; i++) {
    newscale.append(i);
    scale.append(i);
  }

  background(0);
}

public void evolve() {
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

public void draw() {

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

          newnote = PApplet.parseInt(map(all, min, max, smin, smax));
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

              newnote = PApplet.parseInt(map(xy, minxy, maxxy-2, smin, smax));
            } else if (c2mode == 1) {

              newnote = 0;

              boolean b0 = PApplet.parseBoolean(board.getCornerCodeOf(selectX, selectY)[0]);
              boolean b1 = PApplet.parseBoolean(board.getCornerCodeOf(selectX, selectY)[1]);
              boolean b2 = PApplet.parseBoolean(board.getCornerCodeOf(selectX, selectY)[2]);
              boolean b3 = PApplet.parseBoolean(board.getCornerCodeOf(selectX, selectY)[3]);         

              boolean b4 = PApplet.parseBoolean(board.getEdgeCodeOf(selectX, selectY)[0]);
              boolean b5 = PApplet.parseBoolean(board.getEdgeCodeOf(selectX, selectY)[1]);
              boolean b6 = PApplet.parseBoolean(board.getEdgeCodeOf(selectX, selectY)[2]);
              boolean b7 = PApplet.parseBoolean(board.getEdgeCodeOf(selectX, selectY)[3]);

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
                newnote = PApplet.parseInt(map(newnote, 0, 255, smin, smax));
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
              selectX = PApplet.parseInt(t2xyI(curve, rows*columns).x);
              selectY = PApplet.parseInt(t2xyI(curve, rows*columns).y);     

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
public String midi(int val) {

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
public PVector rot(int x, int y, int rx, int ry, int p) {
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

public PVector t2xyI(int t, int p) {
  PVector xy = new PVector(0, 0); // im Ergebnisquadrat die linke untere Ecke
  for (int m = 1; m < p; m *= 2) { // m wächst exponentiell
    int rx = 1 & t/2;      // Binärziffer[1]: 0=links/1=rechts
    int ry = 1 & (t ^ rx); // Binärziffer[0]
    xy = rot(PApplet.parseInt(xy.x), PApplet.parseInt(xy.y), rx, ry, m);
    xy.x += m * rx;
    xy.y += m * ry;
    t /= 4; // zur nächsten Quaternärziffer
  }
  return xy;
}



// ---------------------------------------------------------------------------
// ------------optional manipulation with Mouse and Keyboard------------------
// ---------------------------------------------------------------------------
public void mouseClicked() {
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
// CA
// Cellular Automata
// MIDI generator for monophonic synthesizer
//
// Nguyen Minh Duc
// www.nguyenminhduc.de
// ------------------------------------------------------------------------------
// -----------------------------------------------------------------------------

class Cell {
  int x, y;
  int cellSize;
  int maxstate;
  int state;

  int prevState;

  int lifetime;

  Cell(int posX, int posY, int s, int size, int max) {
    this.x = posX;
    this.y = posY;
    this.cellSize = size;

    this.lifetime = 0;
    this.maxstate = max;
    this.state = s;
    this.prevState = s;
  }

  public void displayColor(int r, int g, int b) {

    fill(r, g, b);
    stroke(0, 200);
    rect(this.x*cellSize, this.y*cellSize, this.cellSize, this.cellSize);
  }

  public void display() {


    fill (PApplet.parseInt(map(this.state, 0, this.maxstate-1, 255, 0)));


    stroke(0);
    rect(this.x*cellSize, this.y*cellSize, this.cellSize, this.cellSize);
  }


  public void next() {

    if ((this.prevState == this.state) && (this.state == 1))
      this.lifetime++;
    else
      this.lifetime = 0;

    this.prevState = this.state;
  }

  public void setState(int s) {
    this.state = s;
  }

  public int getState() {
    return this.state;
  }

  public int getPrevState() {
    return this.prevState;
  }

  public int getLife() {
    return this.lifetime;
  }

  public void initState(int s) {
    this.state = s;
    this.prevState = s;
  }
}
// CA
// Cellular Automata
// MIDI generator for monophonic synthesizer
//
// Nguyen Minh Duc
// www.nguyenminhduc.de
// ------------------------------------------------------------------------------
// -----------------------------------------------------------------------------

class Checkerboard {

  Cell[][] board;
  int rows;
  int columns;
  int cellSize;

  int max;
  int thresh;

  int living;
  int dead;


  Checkerboard(int x, int y, int size, int m) {

    this.board = new Cell[x][y];
    this.columns = x;
    this.rows = y;
    this.cellSize = size;

    this.living = 0;
    this.dead = 0;

    this.max = m;
    this.thresh = 1;
  }


  public void setThresh(int t) {
    this.thresh = t;
  }

  public Cell getCell(int x, int y) {
    return this.board[x][y];
  }

  public int getLiving() {
    return this.living;
  }

  public int getDead() {
    return this.dead;
  }


  public int[] getEdgeCodeOf(int x, int y) {
    int[] e = new int[4];

    if (y == 0)
      e[0] = board[x][this.rows-1].getPrevState();
    else
      e[0] = board[x][y-1].getPrevState();


    if (x == 0)
      e[1] = board[this.columns - 1][y].getPrevState();
    else
      e[1] = board[x-1][y].getPrevState();

    if (y == this.rows - 1)
      e[2] = board[x][0].getPrevState();
    else
      e[2] = board[x][y+1].getPrevState();


    if (x == this.columns - 1)
      e[3] = board[0][y].getPrevState();
    else
      e[3] = board[x+1][y].getPrevState();

    return e;
  }

  public int[] getCornerCodeOf(int x, int y) {
    int[] c = new int[4];
    int loop = 0;

    for (int i = -1; i<=1; i+=2)
      for (int j = -1; j<=1; j+=2) {
        int k = x+i;
        int l = y+j;

        if (k<0) k = this.columns-1;
        else if (k >= this.columns) k = 0;

        if (l<0) l = this.rows-1;
        else if (l >= this.rows) l = 0;

        c[loop] = board[k][l].getPrevState();
        loop++;
      }

    return c;
  }


  public int get2AxialNeighborsOf(int x, int y) {
    int n = 0;
    for (int i = -2; i<=2; i++)
      for (int j = -2; j<=2; j++)
        if ( !((i == 0) && (j == 0)) ) {
          int k = x+i;
          int l = y+j;

          if (k==-1)
            k = this.columns-1;
          else if (k==-2)
            k = this.columns-2;
          else if (k == this.columns)
            k = 0;
          else if (k == this.columns +1 ) 
            k = 1;

          if (l==-1)
            l = this.rows-1;
          else if (l==-2)
            l = this.rows -2;
          else if (l == this.rows)
            l = 0;
          else if (l == this.rows + 1)
            l = 1;

          n += this.board[k][l].getPrevState();
        }

    return n;
  }

  public int getNeighborsOf(int x, int y) {
    int n = 0;
    for (int i = -1; i<=1; i++)
      for (int j = -1; j<=1; j++)
        if ( !((i == 0) && (j == 0)) ) {
          int k = x+i;
          int l = y+j;

          if (k<0)
            k = this.columns-1;
          else if (k >= this.columns)
            k = 0;
          if (l<0)
            l = this.rows-1;
          else if (l >= this.rows)
            l = 0;

          n += this.board[k][l].getPrevState();
        }

    return n;
  }

  public int checkNeighborsValue(int x, int y, int val) {
    int n = 0;
    for (int i = -1; i<=1; i++)
      for (int j = -1; j<=1; j++)
        if ( !((i == 0) && (j == 0)) ) {
          int k = x+i;
          int l = y+j;

          if (k<0)
            k = this.columns-1;
          else if (k >= this.columns)
            k = 0;
          if (l<0)
            l = this.rows-1;
          else if (l >= this.rows)
            l = 0;

          if (this.board[k][l].getPrevState() == val)
            n++;
        }

    return n;
  }

  public int[] getCodeOf(int x, int y) {
    int[] w1 = getEdgeCodeOf(x, y);
    int[] w2 = getCornerCodeOf(x, y);

    int[] code = new int[4];

    for (int i = 0; i < 4; i++)
      code[i] = w1[i] * w2[i];

    return code;
  }

  public void display() {

    int l = 0;
    int d = 0;
    for (int x = 0; x < this.columns; x++)
      for (int y = 0; y < this.rows; y++) {
        this.board[x][y].display();
        this.board[x][y].next();

        if (this.board[x][y].getState()==1)
          l++;
        else
          d++;
      }

    this.dead = d;
    this.living = l;
  }

  public int getSum() {
    int s = 0;
    for (int x = 0; x < this.columns; x++)
      for (int y = 0; y < this.rows; y++)
        s += this.board[x][y].getState();
    return s;
  }

  public void empty() {
    for (int x = 0; x < this.columns; x++)
      for (int y = 0; y < this.rows; y++)
        this.board[x][y] = new Cell(x, y, 0, this.cellSize, this.max);
  }

  public void fillRandom(int k) {

    for (int x = 0; x < this.columns; x++)
      for (int y = 0; y < this.rows; y++)
        this.board[x][y] = new Cell(x, y, PApplet.parseInt(random(k)), this.cellSize, this.max);
  }

  public void evolveXOR(int nhood) {
    for (int x = 0; x < this.columns; x++) {
      for (int y = 0; y < this.rows; y++) {
        int neighbours;
        switch(nhood) {
        case 0:
          // Moore neighbourhood
          neighbours = getNeighborsOf(x, y);
          this.board[x][y].setState((this.board[x][y].getState()+neighbours)%2);
          break;           

        case 1:
          neighbours= 0;
          for (int i = 0; i < 4; i++)
            neighbours += getEdgeCodeOf(x, y)[i];
          this.board[x][y].setState((this.board[x][y].getState()+neighbours)%2);
          break;

        case 2:
          neighbours = get2AxialNeighborsOf(x, y);
          this.board[x][y].setState((this.board[x][y].getState()+neighbours)%2);
          break;
        }
      }
    }
  }
  public void evolveGOL() {
    for (int x = 0; x < this.columns; x++) {
      for (int y = 0; y < this.rows; y++) {

        int neighbors = getNeighborsOf(x, y);

        if ( (this.board[x][y].getPrevState() == 1) && (neighbors < 2) )
          this.board[x][y].setState(0);
        else if ( (this.board[x][y].getPrevState() == 1) && (neighbors > 3) )
          this.board[x][y].setState(0);
        else if ( (this.board[x][y].getPrevState() == 0) && (neighbors == 3) )
          this.board[x][y].setState(1);
      }
    }
  }

  public void evolveDCS() {

    for (int x = 0; x < this.columns; x++) {
      for (int y = 0; y < this.rows; y++) {

        int myval = this.board[x][y].getState();

        if (checkNeighborsValue(x, y, (myval+1)%max) >= this.thresh)
          this.board[x][y].setState((myval+1)%max);
      }
    }
  }
}
// CA
// Cellular Automata
// MIDI generator for monophonic synthesizer
//
// Nguyen Minh Duc
// www.nguyenminhduc.de
// ------------------------------------------------------------------------------
// -----------------------------------------------------------------------------

class ControlFrame extends PApplet {

  int w, h;
  PApplet parent;
  ControlP5 cp5;

  CheckBox checkbox, scalebox;
  RadioButton radio, radio2, midimode, calcmode, calc2mode, selectmode;
  Textarea myTextarea;
  Println console;
  Range range, sumrange;

  public ControlFrame(PApplet _parent, int _w, int _h, String _name) {
    super();   
    parent = _parent;
    w=_w;
    h=_h;
    PApplet.runSketch(new String[]{this.getClass().getName()}, this);
  }

  public void settings() {
    size(w, h);
  }

  public void setup() {
    surface.setLocation(10, 10);
    cp5 = new ControlP5(this);

    cp5.addSlider("automatonDEPTH")
      .plugTo(parent, "depth")
      .setRange(4, 7)
      .setValue(5)
      .setPosition(10, 15)
      .setSize(200, 20)
      .setNumberOfTickMarks(4)
      .setSliderMode(Slider.FLEXIBLE);

    cp5.addSlider("maxstates")
      .plugTo(parent, "maxstates")
      .setRange(2, 10)
      .setValue(2)
      .setPosition(10, 55)
      .setSize(200, 20)
      .setNumberOfTickMarks(9)
      .setSliderMode(Slider.FLEXIBLE);

    cp5.addButton("SET GRID")
      .setPosition(300, 25)
      .setSize(70, 40)
      .setValue(0)
      .setColorBackground(color(205, 0, 0))
      .setId(5);

    cp5.addToggle("SPOUT")
      .plugTo(parent, "shareSpout")
      .setPosition(400, 25)
      .setSize(70, 40)
      .setValue(false)
      .setColorBackground(color(0))
      .setMode(ControlP5.SWITCH);  

    cp5.addToggle("START/STOP")
      .plugTo(parent, "stopping")
      .setPosition(10, 100)
      .setSize(40, 40)
      .setColorBackground(color(0, 255, 0))
      .setValue(false)
      .setId(0);

    cp5.addToggle("MIDI")
      .plugTo(parent, "playMIDI")
      .setPosition(70, 100)
      .setSize(40, 40)
      .setValue(false)
      .lock()
      .setId(6);  

    cp5.addButton("EMTPY")
      .setPosition(130, 100)
      .setSize(40, 40)
      .setValue(0)
      .setId(1);

    cp5.addButton("RANDOM")
      .setPosition(190, 100)
      .setSize(40, 40)
      .setId(2);

    cp5.addSlider("randomamount")
      .setRange(2, 10)
      .setValue(2)
      .setPosition(250, 110)
      .setSize(200, 20)
      .setNumberOfTickMarks(9)
      .setSliderMode(Slider.FLEXIBLE);


    String[] available_outputs = MidiBus.availableOutputs();

    cp5.addScrollableList("MIDIdevice")
      .setPosition(10, 160)
      .setSize(200, 100)
      .setBarHeight(20)
      .setItemHeight(20)
      .addItems(available_outputs);


    cp5.addSlider("MIDIchannel")
      .plugTo(parent, "MIDIchannel")
      .setRange(0, 10)
      .setValue(0)
      .setPosition(220, 160)
      .setSize(200, 20)
      .setNumberOfTickMarks(11)
      .setSliderMode(Slider.FLEXIBLE);

    cp5.addButton("INIT MIDI")
      .setPosition(220, 200)
      .setSize(50, 30)
      .setColorBackground(color(205, 0, 205))
      .setId(3);




    cp5.addSlider("delaytime")
      .plugTo(parent, "delaytime")
      .setRange(0, 1000)
      .setValue(250)
      .setPosition(10, 450)
      .setSize(200, 30);

    cp5.addSlider("inner delaytime")
      .plugTo(parent, "delayinnertime")
      .setRange(25, 1000)
      .setValue(50)
      .setPosition(300, 450)
      .setSize(200, 30);



    cp5.addButton("use SCALE")
      .setPosition(10, 500)
      .setSize(75, 35)
      .setId(8);


    cp5.addButton("reset SCALE")
      .setPosition(610, 505)
      .setSize(75, 35)
      .setId(9);

    checkbox = cp5.addCheckBox("scale")
      .setPosition(100, 510)
      .setSize(20, 20)
      .setItemsPerRow(12)
      .setSpacingColumn(20)
      .setSpacingRow(20)
      .addItem("C", 0)
      .addItem("C#", 1)
      .addItem("D", 2)
      .addItem("D#", 3)
      .addItem("E", 4)
      .addItem("F", 5)
      .addItem("F#", 6)
      .addItem("G", 7)
      .addItem("G#", 8)
      .addItem("A", 9)
      .addItem("A#", 10)
      .addItem("B", 11)
      .activateAll()
      .setNoneSelectedAllowed(false)
      .setId(4);

    scalebox = cp5.addCheckBox("scalebox")
      .setPosition(10, 550)
      .setSize(13, 13)
      .setItemsPerRow(20)
      .setSpacingColumn(20)
      .setSpacingRow(5);

    for (int i = 0; i < 128; i++)
      scalebox.addItem(i+"", i);

    scalebox.activateAll();


    radio = cp5.addRadioButton("automationmode")
      .setPosition(10, 300)
      .setSize(40, 30)
      .setColorForeground(color(120))
      .setColorActive(color(255, 255, 0))
      .setColorLabel(color(255))
      .setItemsPerRow(1)
      .setSpacingRow(20)
      .addItem("GOL", 0)
      .addItem("XOR", 1)
      .addItem("DCS", 2)
      .setNoneSelectedAllowed(false)
      .activate(0);

    for (Toggle t : radio.getItems()) {
      t.getCaptionLabel().setColorBackground(color(255, 80));
      t.getCaptionLabel().getStyle().moveMargin(-7, 0, 0, -3);
      t.getCaptionLabel().getStyle().movePadding(7, 0, 0, 3);
      t.getCaptionLabel().getStyle().backgroundWidth = 45;
      t.getCaptionLabel().getStyle().backgroundHeight = 13;
    }
    radio2 = cp5.addRadioButton("neighbour")
      .setPosition(130, 360)
      .setSize(15, 15)
      .setColorForeground(color(120))
      .setColorActive(color(100, 200, 200))
      .setColorLabel(color(255))
      .setItemsPerRow(3)
      .setSpacingColumn(60)
      .addItem("Moore", 0)
      .addItem("Neumann", 1)
      .addItem("2-Axial", 2)
      .setNoneSelectedAllowed(false)
      .activate(0);

    cp5.addSlider("thresh")
      .setRange(1, 10)
      .setValue(1)
      .setPosition(130, 405)
      .setSize(200, 15)
      .setNumberOfTickMarks(10)
      .setSliderMode(Slider.FLEXIBLE);

    cp5.addButton("setthresh")
      .setPosition(390, 405)
      .setSize(40, 20)
      .setColorBackground(color(150, 150, 50))
      .setId(7);

    midimode = cp5.addRadioButton("midimode")
      .setPosition(10, 705)
      .setSize(40, 30)
      .setColorForeground(color(120))
      .setColorActive(color(0, 255, 255))
      .setColorLabel(color(255))
      .setItemsPerRow(1)
      .setSpacingRow(20)
      .addItem("SUM", 0)
      .addItem("SINGLE", 1)
      .setNoneSelectedAllowed(false)
      .activate(0);

    calcmode = cp5.addRadioButton("calcmode")
      .setPosition(100, 710)
      .setSize(20, 20)
      .setColorForeground(color(120))
      .setColorActive(color(155, 105, 220))
      .setColorLabel(color(255))
      .setItemsPerRow(2)
      .setSpacingColumn(50)
      .addItem("MAP", 0)
      .addItem("MOD", 1)
      .setNoneSelectedAllowed(false)
      .activate(0);

    calc2mode = cp5.addRadioButton("calc2mode")
      .setPosition(100, 760)
      .setSize(20, 20)
      .setColorForeground(color(120))
      .setColorActive(color(155, 105, 220))
      .setColorLabel(color(255))
      .setItemsPerRow(2)
      .setSpacingColumn(55)
      .addItem("MIRANDA", 0)
      .addItem("NEIGHBOUR", 1)
      .setNoneSelectedAllowed(false)
      .activate(0);

    selectmode = cp5.addRadioButton("selectmode")
      .setPosition(350, 760)
      .setSize(20, 20)
      .setColorForeground(color(120))
      .setColorActive(color(155, 205, 120))
      .setColorLabel(color(255))
      .setItemsPerRow(3)
      .setSpacingColumn(55)
      .addItem("ROW", 0)
      .addItem("COLUMN", 1)
      .addItem("HILBERT", 2)
      .setNoneSelectedAllowed(false)
      .activate(0);

    range = cp5.addRange("range")
      // disable broadcasting since setRange and setRangeValues will trigger an event
      .setBroadcast(false)
      .setPosition(250, 720)
      .setSize(400, 15)
      .setHandleSize(20)
      .setRange(0, 1)
      .setRangeValues(0, 1)
      // after the initialization we turn broadcast back on again
      .setBroadcast(true)
      .setColorForeground(color(255, 0, 0, 200))
      .setColorBackground(color(100, 0, 0, 100))  
      .setId(11)
      ;

    sumrange = cp5.addRange("sumrange")
      // disable broadcasting since setRange and setRangeValues will trigger an event
      .setBroadcast(false)
      .setPosition(250, 690)
      .setSize(400, 15)
      .setHandleSize(20)
      .setRange(0, 1)
      .setRangeValues(0, 1)
      // after the initialization we turn broadcast back on again
      .setBroadcast(true)
      .setColorForeground(color(255, 0, 0, 200))
      .setColorBackground(color(100, 0, 0, 100))
      .setId(12)
      ;

    cp5.addToggle("repeating")
      .plugTo(parent, "noRepeat")
      .setPosition(600, 750)
      .setSize(30, 30)
      .setValue(true)
      .setId(10)
      .setMode(ControlP5.SWITCH);  


    myTextarea = cp5.addTextarea("txt")
      .setPosition(0, 800)
      .setSize(800, 175)
      .setFont(createFont("Arial", 12))
      .setLineHeight(14)
      .setColor(color(200))
      .setColorBackground(color(0))
      .setColorForeground(color(255));

    console = cp5.addConsole(myTextarea);
  }

  public void useScale(boolean value) {
    int sumscale = 0;

    if (value==true) {
      for (int i=0; i<checkbox.getArrayValue().length; i++) 
        sumscale+=checkbox.getArrayValue()[i];

      if (sumscale == 0) 
        checkbox.activate(0);
    }
  }

  public void controlEvent(ControlEvent theEvent) {
    if (theEvent.isFrom(radio)) {
      for (int i=0; i<theEvent.getGroup().getArrayValue().length; i++) {
        if (theEvent.getGroup().getArrayValue()[i]==1)
          newmode = i;
      }

      println("# set automation mode to : "+newmode);
      newvalues=true;
    }

    if (theEvent.isFrom(radio2)) {
      for (int i=0; i<theEvent.getGroup().getArrayValue().length; i++) {
        if (theEvent.getGroup().getArrayValue()[i]==1)
          newnmode = i;
      }
      println("# set neighbourhood to : "+newnmode);
      newvalues=true;
    }

    if (theEvent.isFrom(midimode)) {
      for (int i=0; i<theEvent.getGroup().getArrayValue().length; i++) {
        if (theEvent.getGroup().getArrayValue()[i]==1)
          newmmode = i;
      }
      println("# set midi convention (SUM or SINGLE) to : "+newmmode);
      newvalues=true;
    }

    if (theEvent.isFrom(calcmode)) {
      for (int i=0; i<theEvent.getGroup().getArrayValue().length; i++) {
        if (theEvent.getGroup().getArrayValue()[i]==1)
          newcmode = i;
      }
      println("# set mapping mode (MAP or MOD) to : "+newcmode);
  newvalues=true;  
  }

    if (theEvent.isFrom(calc2mode)) {
      for (int i=0; i<theEvent.getGroup().getArrayValue().length; i++) {
        if (theEvent.getGroup().getArrayValue()[i]==1)
          newc2mode = i;
      }
      println("# set MIDI generation (MIRANDA or NEIGHBOURHOOD) to : "+newc2mode);
      newvalues=true;
    }

    if (theEvent.isFrom(selectmode)) {
      for (int i=0; i<theEvent.getGroup().getArrayValue().length; i++) {
        if (theEvent.getGroup().getArrayValue()[i]==1)
          newsmode = i;
      }
      println("# set selecting order to : "+newsmode);
      newvalues=true;
    }

    if (theEvent.getId() != -1) {


      switch(theEvent.getId()) {
        case(0):
        //START STOP BUTTON
        if (stopping)
          println("# start AUTOMATION");
        else 
        println("# stop AUTOMATION");
        break;

        case(1): 
        //EMPTY BUTTON
        board.empty();
        update = true;
        println("# empty grid");
        break;

        case(2): 
        //RANDOM BUTTON
        int r = PApplet.parseInt(cp5.getController("randomamount").getValue());
        if (r <= maxstates) {
          board.fillRandom(r);
          update = true;
          println("# randomize grid with value: "+r);
        } else println("# random value must be smaller then MAXSTATES");
        break;

        case(3):
        //MIDI INIT BUTTON
        int device = PApplet.parseInt(cp5.getController("MIDIdevice").getValue());
        myBus = new MidiBus(this, -1, device); 
        println("# init MIDI with OUTPUT "+device);
        println("# init MIDI with CHANNEL "+ PApplet.parseInt(cp5.getController("MIDIchannel").getValue()));
        cp5.getController("MIDI").unlock();
        break;

        case(4):
        //ADD SCALE TONE
        for (int i=0; i<checkbox.getArrayValue().length; i++) {
          if (checkbox.getArrayValue()[i] == 1) {
            for (int j = 0; j < 12; j++)
              if (i+(j*12)<128)
                scalebox.activate(i+(j*12));
          } else {
            for (int j = 0; j < 12; j++)
              if (i+(j*12)<128)
                scalebox.deactivate(i+(j*12));
          }
        }
        break;

        case(5):
        //INIT GRID BUTTON
        rows = PApplet.parseInt(pow(2, depth));
        columns = PApplet.parseInt(pow(2, depth));
        size = PApplet.parseInt(40 / pow(2, (depth-4)));
        println("# grid size: "+size);
        println("# rows : "+rows);
        println("# columns : "+columns);
        board = new Checkerboard(columns, rows, size, maxstates);
        board.fillRandom(0);
        update = true;
        maxsum = rows*columns*(maxstates-1);
        println("# init grid and automaton");
        break;

        case(6):
        //MIDI ON OFF BUTTON
        if (cp5.getController("MIDI").isActive()  == false)
          for (int val = 0; val < 128; val++)
            myBus.sendNoteOff(MIDIchannel, val, 127);
        break;

        case(7):
        //TRESH BUTTON
        int t = PApplet.parseInt(cp5.getController("thresh").getValue());
        board.setThresh(t);
        println("# set threshhold for DCS automation to: " + t);
        break;

        case(8):
        //USE SCALE BUTTON
        int s = 0; 
        for (int i=0; i<scalebox.getArrayValue().length; i++) 
          s+=scalebox.getArrayValue()[i];

        if (s != 0) {
          newscale.clear();
          float[] f = scalebox.getArrayValue();
          for (int i = 0; i < 128; i++)
            if (f[i] == 1)
              newscale.append(i);
          newscalesize = newscale.size()-1;
          newvalues=true;
          print("# set scale: ");
          printArray(newscale);
        } else println("scale CAN NOT BE EMPTY");
        break;

        case(9):
        //RESET SCALE BUTTON
        int sumscale = 0; 
        for (int i=0; i<scalebox.getArrayValue().length; i++) 
          sumscale+=scalebox.getArrayValue()[i];

        if (sumscale == 0) {
          checkbox.activateAll();
          scalebox.activateAll();
        } else { 
          checkbox.deactivateAll();
          scalebox.deactivateAll();
        }
        break;

        case(10):
        //NOREPEAT BUTTON
        println("# no repeating notes : "+noRepeat);
        break;

        case(11):
        //RANGE 
        newmapbegin = (cp5.getController("range").getArrayValue(0));
        newmapend = (cp5.getController("range").getArrayValue(1));
        newvalues=true;
        println("# range update, done : min: "+newmapbegin + ", max: "+newmapend);
        break;

        case(12):
        //SUM RANGE
        newsumbegin = (cp5.getController("sumrange").getArrayValue(0));
        newsumend = (cp5.getController("sumrange").getArrayValue(1));
        newvalues=true;
        println("# sumrange update, done : min: "+newsumbegin + ", max: "+newsumend);
        break;
      }
    }
  }


  public void draw() {
    background(100);

    if (board!=null) {

      fill(0);
      rect(0, height-25, width, 25);
      fill(255);
      text("frames: "+count, 10, height-7);
      text("living: "+board.getLiving(), 100, height-7);
      text("sum: "+board.getSum(), 200, height-7);
      text("maxvalues: "+maxsum, 300, height-7);
      text("value: "+val, 400, height-7);
      text("scalelength: "+scalesize, 500, height-7);
      //text("dead: "+board.getDead(),10,height - 10);
      //text("living: "+board.getLiving(),110,height - 10);
    }
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "voixtotale" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
