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


  void setThresh(int t) {
    this.thresh = t;
  }

  Cell getCell(int x, int y) {
    return this.board[x][y];
  }

  int getLiving() {
    return this.living;
  }

  int getDead() {
    return this.dead;
  }


  int[] getEdgeCodeOf(int x, int y) {
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

  int[] getCornerCodeOf(int x, int y) {
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


  int get2AxialNeighborsOf(int x, int y) {
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

  int getNeighborsOf(int x, int y) {
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

  int checkNeighborsValue(int x, int y, int val) {
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

  int[] getCodeOf(int x, int y) {
    int[] w1 = getEdgeCodeOf(x, y);
    int[] w2 = getCornerCodeOf(x, y);

    int[] code = new int[4];

    for (int i = 0; i < 4; i++)
      code[i] = w1[i] * w2[i];

    return code;
  }

  void display() {

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

  int getSum() {
    int s = 0;
    for (int x = 0; x < this.columns; x++)
      for (int y = 0; y < this.rows; y++)
        s += this.board[x][y].getState();
    return s;
  }

  void empty() {
    for (int x = 0; x < this.columns; x++)
      for (int y = 0; y < this.rows; y++)
        this.board[x][y] = new Cell(x, y, 0, this.cellSize, this.max);
  }

  void fillRandom(int k) {

    for (int x = 0; x < this.columns; x++)
      for (int y = 0; y < this.rows; y++)
        this.board[x][y] = new Cell(x, y, int(random(k)), this.cellSize, this.max);
  }

  void evolveXOR(int nhood) {
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
  void evolveGOL() {
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

  void evolveDCS() {

    for (int x = 0; x < this.columns; x++) {
      for (int y = 0; y < this.rows; y++) {

        int myval = this.board[x][y].getState();

        if (checkNeighborsValue(x, y, (myval+1)%max) >= this.thresh)
          this.board[x][y].setState((myval+1)%max);
      }
    }
  }
}
