package maze;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import hw3.Maze;
import hw3.MazeCell;

/**
 * UI for a maze search application.  A client (normally the
 * main thread in an application) will periodically call
 * updateStatus to trigger a repaint of the maze.  The maze
 * data is shared between the UI and the main thread.
 */
public class MazePanel extends JPanel implements MazeObserver
{
  /**
   * Size in pixels of the cells for the grid.
   */
  public static final int cellSize = 20;
  
  /**
   * The grid to be displayed by this panel.
   */
  protected Maze maze;
  protected boolean ready;
  protected int sleepTime;
  
  private Scanner in = new Scanner(System.in);
  private boolean started = false;
  /**
   */
  public MazePanel(Maze maze, int sleepTime)
  {
    this.maze = maze;
    this.sleepTime = sleepTime;
  }

  // NOTE: this is called from the main thread when the maze
  // has been changed, so all we really need to do is trigger
  // a repaint.  However, to create a reasonable animation
  // speed we cause the calling thread to sleep for a 
  // few milliseconds before returning.
  @Override
  public void updateStatus(MazeCell cell)
  {
//    if (!started)
//    {
//      try
//      {
//        Thread.sleep(15000);
//      }
//      catch(InterruptedException e)
//      {
//        
//      }
//
//      started = true;
//    }
    repaint();
    try
    {
      Thread.sleep(sleepTime);
    }
    catch(InterruptedException e)
    {
      
    }
    //in.nextLine();
  }

  @Override
  public void paintComponent(Graphics g)
  {
    // clear background
    g.clearRect(0, 0, getWidth(), getHeight());
    
    for (int row = 0; row < maze.getRows(); ++row)
    {
      for (int col = 0; col < maze.getColumns(); ++col)
      {
        Color color = getColor(maze.getCell(row, col));
        g.setColor(color);
        g.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
      }
    }
    
    // draw grid
    g.setColor(Color.WHITE);
    for (int row = 0; row < maze.getRows(); ++row)
    {
      for (int col = 0; col < maze.getColumns(); ++col)
      {
        g.drawRect(col * cellSize, row * cellSize, cellSize, cellSize);
      }
    }

   }
  
  /**
   * Returns a color for cells with the given Status.
   * @param status
   * @return
   */
  private Color getColor(MazeCell m)
  {
    if (maze.getStart().equals(m)) return Color.BLUE;
    switch(m.getStatus())
    {  
      case WALL: return Color.BLACK;
      case UNVISITED: return Color.LIGHT_GRAY;
      case VISITED: return Color.ORANGE;
      case DEAD: return Color.RED;
      case SUCCEEDED: return Color.GREEN;
      case GOAL: return Color.YELLOW;
    }
    return Color.WHITE;
  }
  
  protected static void createAndShow(final CountDownLatch latch, final Maze maze, final int sleepTime)
  {

    // create the frame
    JFrame frame = new JFrame("Backtracker");
    
    // create an instance of our JPanel subclass and 
    // add it to the frame
    MazePanel panel = new MazePanel(maze, sleepTime);
    frame.getContentPane().add(panel);
    panel.setPreferredSize(new Dimension(maze.getColumns() * cellSize, maze.getRows() * cellSize));
    // give it a nonzero size
    frame.pack();
    //frame.setSize(300, 100);
    
    // we want to shut down the application if the 
    // "close" button is pressed on the frame
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    // make the frame visible and start the UI machinery
    frame.setVisible(true);   
    maze.setObserver(panel);
    latch.countDown();
  }

  public static void start(final Maze maze, final int sleepTime)
  {
    final CountDownLatch latch = new CountDownLatch(1);
    Runnable r = new Runnable()
    {
      public void run()
      {
        createAndShow(latch, maze, sleepTime);
      }
    };
    SwingUtilities.invokeLater(r);
    try
    {
      latch.await();
    }
    catch (InterruptedException ie){}
  }
}
