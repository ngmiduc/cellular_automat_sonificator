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

  void useScale(boolean value) {
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
        int r = int(cp5.getController("randomamount").getValue());
        if (r <= maxstates) {
          board.fillRandom(r);
          update = true;
          println("# randomize grid with value: "+r);
        } else println("# random value must be smaller then MAXSTATES");
        break;

        case(3):
        //MIDI INIT BUTTON
        int device = int(cp5.getController("MIDIdevice").getValue());
        myBus = new MidiBus(this, -1, device); 
        println("# init MIDI with OUTPUT "+device);
        println("# init MIDI with CHANNEL "+ int(cp5.getController("MIDIchannel").getValue()));
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
        rows = int(pow(2, depth));
        columns = int(pow(2, depth));
        size = int(40 / pow(2, (depth-4)));
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
        int t = int(cp5.getController("thresh").getValue());
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


  void draw() {
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
