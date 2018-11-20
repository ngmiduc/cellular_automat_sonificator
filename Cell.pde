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

  void displayColor(int r, int g, int b) {

    fill(r, g, b);
    stroke(0, 200);
    rect(this.x*cellSize, this.y*cellSize, this.cellSize, this.cellSize);
  }

  void display() {


    fill (int(map(this.state, 0, this.maxstate-1, 255, 0)));


    stroke(0);
    rect(this.x*cellSize, this.y*cellSize, this.cellSize, this.cellSize);
  }


  void next() {

    if ((this.prevState == this.state) && (this.state == 1))
      this.lifetime++;
    else
      this.lifetime = 0;

    this.prevState = this.state;
  }

  void setState(int s) {
    this.state = s;
  }

  int getState() {
    return this.state;
  }

  int getPrevState() {
    return this.prevState;
  }

  int getLife() {
    return this.lifetime;
  }

  void initState(int s) {
    this.state = s;
    this.prevState = s;
  }
}
