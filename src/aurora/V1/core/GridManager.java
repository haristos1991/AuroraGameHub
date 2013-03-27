/*
 * Made By Sardonix Creative.
 *
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit
 *
 *      http://creativecommons.org/licenses/by-nc-nd/3.0/
 *
 * or send a letter to Creative Commons, 444 Castro Street, Suite 900,
 * Mountain View, California, 94041, USA.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aurora.V1.core;

import aurora.engine.V1.UI.ADialog;
import aurora.engine.V1.UI.AGridPanel;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JComponent;
import org.apache.log4j.Logger;

/**
 * GridManager An array of aGridPanels Provides methods to manipulate and get
 * Grids
 *
 * @author Sammy
 */
public class GridManager {

    private AuroraCoreUI ui;

    private int row;

    private int col;

    private ArrayList<AGridPanel> Grids = new ArrayList<AGridPanel>();

    private int fullGrids;

    private GamePlaceholder blankAddGame;

    private ActionListener listener;

    private GamePlaceholder placeholder;

    private int width;

    private int height;

    private boolean needFinalizing = false;

    private boolean isTransitioningGame;

    private int visibleGrid;

    static final Logger logger = Logger.getLogger(GridManager.class);

    /**
     * Manages GridPanels for GameLibrary
     *
     * @param row
     * @param col
     * @param ui
     */
    public GridManager(int row, int col, AuroraCoreUI ui) {
        this.row = row;
        this.col = col;
        this.ui = ui;
        this.visibleGrid = 0;
    }

    GridManager() {
    }

    /**
     * initiate a default grid
     *
     * @param index Specific position to add Grid to Manager
     */
    public void initiateGrid(int index) {

        createGrid(row, col, index);

    }

    /**
     * Add a game to Grid
     *
     * @param GameCover object
     */
    public void addGame(Game game) {
        fullGrids = 0;
        for (int i = 0; i < Grids.size(); i++) {
            if (!isDupicate(game) || isTransitioningGame) {

                if (!Grids.get(i).isGridFull()) {

                    Grids.get(i).addToGrid(game);
                    isTransitioningGame = false; // Is Not Being Added to next Grid

                    if (logger.isDebugEnabled()) {
                        logger.debug("Added Game To GridPanel: " + game
                                .getName());
                        logger.debug("to Grid " + i);
                    }

                } else if (containsPlaceHolders(Grids.get(i))) {

                    replacePlaceHolder(Grids.get(i), game, listener);

                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("FAILED To add: " + game.getName());
                        logger.debug("Grid " + i + " is Full!");
                    }

                    fullGrids++;
                    //when Full make new Grid
                    if (fullGrids == Grids.size()) {
                        createGrid(row, col, Grids.size());
                        Grids.get(Grids.size() - 1).addToGrid(game);
                        isTransitioningGame = true; // Is Being Added to next Grid
                        if (logger.isDebugEnabled()) {
                            logger.debug("Added Game: " + game.getName());
                            logger.debug("to Grid " + (Grids.size() - 1));
                        }

                    }
                }
            } else {
                ADialog info = new ADialog(ADialog.aDIALOG_WARNING,
                        "Cannot Add Duplicate Box Art", ui.getDefaultFont()
                        .deriveFont(25));
                info.showDialog();
                info.setVisible(true);
                echoGame(game).selected();
            }

        }


    }

    /**
     * check if Game Cover Art is already in the library
     *
     * @param game GameCover to check for duplicates
     */
    public boolean isDupicate(Game game) {
        for (int i = 0; i < Grids.size(); i++) {
            for (int a = 0; a < Grids.get(i).getArray().size(); a++) {
                if (Grids.get(i).getArray().get(a) instanceof GamePlaceholder
                    == false) {
                    Game cover = (Game) Grids.get(i).getArray().get(a);
                    if (cover.getBoxArtUrl().equals(game.getBoxArtUrl())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Finds Same Game and returns that game in the library
     *
     * @param game
     *             <
     *             p/>
     * <p/>
     * @return
     */
    public Game echoGame(Game game) {

        for (int i = 0; i < Grids.size(); i++) {
            for (int a = 0; a < Grids.get(i).getArray().size(); a++) {
                if (Grids.get(i).getArray().get(a) instanceof GamePlaceholder
                    == false) {

                    Game cover = (Game) Grids.get(i).getArray().get(a);
                    if (cover.getBoxArtUrl().equals(game.getBoxArtUrl())) {
                        return cover;
                    }
                }
            }
        }
        return null;

    }

    public void finalizeGrid(ActionListener listener, int width, int height) {
        this.listener = listener;

        this.width = width;
        this.height = height;

        if (!Grids.get(Grids.size() - 1).isGridFull()) {



            this.blankAddGame = new GamePlaceholder();
            blankAddGame.setUp(width + 10, height + 10,
                    "library_placeholder_bg.png");
            blankAddGame.addButton("library_placeholder_add_norm.png",
                    "library_placeholder_add_down.png",
                    "library_placeholder_add_over.png", listener);
            Grids.get(Grids.size() - 1).addToGrid(blankAddGame);

        }

        addPlaceHolders(width, height);
    }

    /**
     * Adds Placeholder Items instead of Games using predefined image heights
     * and widths
     *
     * @param width
     * @param height
     */
    public void addPlaceHolders(int width, int height) {
        while (!Grids.get(Grids.size() - 1).isGridFull()) {
            this.placeholder = new GamePlaceholder();
            placeholder.setUp(width + 10, height + 10,
                    "library_placeholder_bg.png");

            Grids.get(Grids.size() - 1).addToGrid(placeholder);
        }
    }

    /**
     * check if any other cover was selected and sets it to unselected
     */
    public void unselectPrevious() {
        for (int i = 0; i < Grids.size(); i++) {
            for (int j = 0; j < Grids.get(i).getArray().size(); j++) {
                if (!(Grids.get(i).getArray().get(j) instanceof GamePlaceholder)) {
                    Game game = (Game) Grids.get(i).getArray().get(j);
                    if (game.isSelected()) {
                        game.unselected();
                        game.getGameBar().setVisible(false);
                        game.revalidate();
                    }
                }
            }

        }
    }
//attempts to remove everything in grid.

    public void clearAllGrids() {
        for (int i = 0; i < Grids.size(); i++) {

            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Clearing Grid... " + i);
                }

                Grids.get(i).getArray().clear();
                Grids.get(i).update();
                Grids.get(i).revalidate();


            } catch (RuntimeException ex) {
                logger.error(ex);
            }

        }
    }

    private void replacePlaceHolder(AGridPanel gridPanel, Game game,
                                    ActionListener addGameHandler) {

        //Replace placeholder with Game then add placeholder at the end
        //using finilize

        for (int a = (gridPanel.getArray().size() - 1); a >= 0; a--) {
            if (!(gridPanel.getArray().get(a) instanceof Game)) {
                gridPanel.removeComp((JComponent) gridPanel.getArray().get(a));
                gridPanel.update();
            }
        }

        gridPanel.addToGrid(game);
        gridPanel.update();
        this.finalizeGrid(addGameHandler, game.getWidth(), game.getHeight());
        gridPanel.update();
    }

    /**
     * find a game in any Grid int[0] = Grid int[1] = GridPosition
     *
     * @param GameCover object
     */
    public int[] findGame(Game game) {
        int GridPosition = -1;
        int Grid = -1;
        for (int i = 0; i < Grids.size(); i++) {
            if (Grids.get(i).find(game) != -1) {
                GridPosition = Grids.get(i).find(game);
                Grid = i;
            }
        }

        int[] find = new int[2];
        find[0] = Grid;
        find[1] = GridPosition;

        return find;
    }

    public boolean gameExists(Game game) {

        for (int i = 0; i < Grids.size(); i++) {
            if (logger.isDebugEnabled()) {
                logger.debug("GameName " + game.getName());
                logger.debug(Grids.get(i).find(game));
            }

            if (Grids.get(i).find(game) != -1) {
                return true;
            }
        }

        return false;
    }

    private boolean containsPlaceHolders(AGridPanel gridPanel) {

        for (int i = 0; i < Grids.size(); i++) {
            for (Object game : gridPanel.getArray()) {
                if (game instanceof GamePlaceholder) {
                    return true;
                }
            }
        }
        return false;

    }

    private boolean containsAddPlaceHolders(AGridPanel gridPanel) {

        for (int i = 0; i < Grids.size(); i++) {
            for (Object obj : gridPanel.getArray()) {
                if (obj instanceof GamePlaceholder) {
                    GamePlaceholder place = (GamePlaceholder) obj;
                    if (place.isContainsButton()) {
                        return true;
                    }
                }
            }
        }
        return false;

    }

    public int getVisibleGridIndex() {
        return visibleGrid;
    }

    public void decrementVisibleGridIndex() {
        visibleGrid--;
    }

    public void incrementVisibleGridIndex() {
        visibleGrid++;
    }

    /**
     * find a game in any Grid
     *
     * int[0] = Grid int[1] = GridPosition
     *
     * @param GameCover object
     */
    public int[] findGameName(String Name) {
        int Grid = -1;
        int GridPosition = -1;

        //Go Through all Grids
        for (int i = 0; i < Grids.size(); i++) {
            //Go Through All panels in Each Grid

            for (int a = 0; a < Grids.get(i).getArray().size(); a++) {
                try {
                    Game game = (Game) Grids.get(i).getArray().get(a);
                    if (game.getName().equalsIgnoreCase(Name)) {

                        Grid = i;
                        GridPosition = a;
                    }
                } catch (RuntimeException ex) {
                }
            }
        }

        int[] find = new int[2];
        find[0] = Grid;
        find[1] = GridPosition;

        return find;
    }

    /**
     * .-----------------------------------------------------------------------.
     * | getGameFromName(String)
     * .-----------------------------------------------------------------------.
     * | Returns the Game object if its in the Library by searching through
     * | all of the grids.
     * .........................................................................
     *
     * <p/>
     */
    public Game getGameFromName(String GameName) {
        Game gameFound = null;

        try{
        gameFound = (Game) this.getGrid(this.findGameName(GameName)[0])
                .getArray().get(this.findGameName(GameName)[1]);
        }catch(Exception ex){
            gameFound = null;
        }

        return gameFound;

    }

    /**
     * removes a game in any Grid
     *
     * @param GameCover object
     */
    public void removeGame(Game game) {

        //TODO support appostrophe removal

        // get the grid location of where the game is contained
        int[] gridLocation = this.findGame(game);

        if (logger.isDebugEnabled()) {
            logger.debug("Game as found in grid location: " + gridLocation[0]
                         + "," + gridLocation[1]);
        }

        // grab the index of where the grid is located in the manager
        int index = gridLocation[0];

        // get the grid where the game is located
        AGridPanel grid = this.getGrid(index);


        // alternative to remove the game
        grid.removeComp(game);

        System.out.println("Number of grids that exist: " + Grids.size());

        if ((Grids.size() - 1) > index) {

            if (logger.isDebugEnabled()) {
                logger.debug("grid.size is > index");
            }

            for (int i = index; i < Grids.size() - 1; i++) {
                AGridPanel currentGrid = this.getGrid(i);
                AGridPanel nextGrid = this.getGrid(i + 1);
                Object o = nextGrid.getFirstComponent();

                if (o.getClass().equals(game.getClass())) {
                    nextGrid.removeComp((Game) o);
                    nextGrid.update();
                    currentGrid.addToGrid((Game) o);
                    currentGrid.update();
                } else {
                    Grids.remove(nextGrid);
                    if (!containsAddPlaceHolders(grid)) {
                        needFinalizing = true;
                    }
                }

            }

            // finalize the grid if there is no placeholder and it is the last grid in
            // in the library
        } else if (((Grids.size() - 1) == index) && (!containsAddPlaceHolders(
                grid))) {

            needFinalizing = true;

        }

        if (needFinalizing) {

            finalizeGrid(listener, width, height);
            needFinalizing = false;
        } else {
            addPlaceHolders(game.getWidth(), game.getHeight());
        }

        grid.update();

    }
    
    /**
     * removes a game in any Grid
     *
     * @param GameCover object
     */
    public void moveGame(Game game) {

        // get the grid location of where the game is contained
        int[] gridLocation = this.findGame(game);
        
        if (logger.isDebugEnabled()) {
        	logger.debug("Game was found in grid location: " + gridLocation[0]
                    + "," + gridLocation[1]);
        }

        // grab the index of where the grid is located in the manager
        int index = gridLocation[0];
        
        if (logger.isDebugEnabled()) {
        	logger.debug("Game was found in index: " + index);
        }

        // get the grid where the game is located
        AGridPanel grid = this.getGrid(index);

        System.out.println("Number of grids that exist: " + Grids.size());
       
       if (index == 0) {
    	   grid.removeComp(game);
    	   grid.addToGrid(game, 0);
    	   grid.update();  	   
       }
        
        if (index > 0) {
        	// alternative to remove the game
            grid.removeComp(game);
        	
            AGridPanel firstGrid = this.getGrid(0);
        	//Game lastGame = (Game) firstGrid.getComponent(7);
        	
        	
        	//firstGrid.removeComp(lastGame);
        	//firstGrid.addToGrid(game, 0);
        	//firstGrid.update();
        	
        	//grid.addToGrid(lastGame, 0);
        	//grid.update();
        	
        	for (int i = index - 1; i >= 0; i--) {
        		System.out.println("Index = " + i);
        		System.out.println("Index + 1 = " + i + 1);
        		AGridPanel currentGrid = this.getGrid(i);
        		AGridPanel previousGrid = this.getGrid(i+1);
        		Game lastGame = (Game) currentGrid.getComponent(7);
        		currentGrid.removeComp(lastGame);
        		currentGrid.update();
        		previousGrid.addToGrid(lastGame, 0);
        		previousGrid.update();      		
        	}
        	
        	firstGrid.addToGrid(game, 0);
        	firstGrid.update();
        	
        	
        	//firstGrid.removeComp(lastGame);
        	//firstGrid.addToGrid(game, 0);
        	//firstGrid.update();
         	
        	/*for (int i = 1; i < Grids.size() - 1; i++) {
        		if (i == index)
        		AGridPanel currentGrid = this.getGrid(i);
        		
        		if (currentGrid.isGridFull()) {
        			Game cgLastGame = (Game) currentGrid.getComponent(7);
        			
        		}
        		
        	}*/
        	
        	/*for (int i = 0; i < Grids.size() - 1; i++) {
            	
            	if (i + 1 < Grids.size()) {
            		AGridPanel currentGrid = this.getGrid(i);
            		
                	Game lastGame = (Game) currentGrid.getComponent(7);
            		AGridPanel nextGrid = this.getGrid(i + 1);
            		currentGrid.removeComp(lastGame);
                	currentGrid.update();
                	nextGrid.addToGrid(lastGame, 0);
                	nextGrid.update();
            	}
            	
            }*/
        	
        }
       
        
        

        // if the grid is not the last grid
  /*      if ((Grids.size() - 1) > index) {
        	
        	if (logger.isDebugEnabled()) {
        		logger.debug("grid.size is > index");
        	}
        	
            for (int i = index; i < Grids.size() - 1; i++) {
                AGridPanel currentGrid = this.getGrid(i);
                AGridPanel nextGrid = this.getGrid(i + 1);
                Object o = nextGrid.getFirstComponent();

                if (o.getClass().equals(game.getClass())) {
                    nextGrid.removeComp((Game) o);
                    nextGrid.update();
                    currentGrid.addToGrid((Game) o);
                    currentGrid.update();
                } else {
                    Grids.remove(nextGrid);
                    if (!containsAddPlaceHolders(grid)) {
                        needFinalizing = true;
                    }
                }

            }
         
            // finalize the grid if there is no placeholder and it is the last grid in
            // in the library
        }*//* else if (((Grids.size() - 1) == index) && (!containsAddPlaceHolders(grid))) {

            needFinalizing = true;

        } 

        if (needFinalizing) {

            finalizeGrid(listener, width, height);
            needFinalizing = false;
        } else {
            addPlaceHolders(game.getWidth(), game.getHeight());
        }*/

        grid.update();

    }

    /**
     * Create and add a new GridPanel to the Grids ArrayList
     *
     * @param row   Row Number
     * @param col   Column Number
     * @param index specific position to add new Grid
     *
     */
    public void createGrid(int row, int col, int index) {
        AGridPanel GridPanel = new AGridPanel(row, col, true);

        Grids.add(index, GridPanel);
    }

    /**
     * Returns AGridPanel which may contain JComponents in a grid
     *
     * @param PanelIndex
     *                   <
     *                   p/>
     * <p/>
     * @return AGridPanel
     *
     */
    public AGridPanel getGrid(int panelIndex) {

        return Grids.get(panelIndex);

    }

    /**
     * check if any other cover was selected and sets it to unselected
     */
    public AGridPanel getSelectedGrid() {
        for (int i = 0; i < Grids.size(); i++) {
            for (int j = 0; j < Grids.get(i).getArray().size(); j++) {
                try {
                    Game game = (Game) Grids.get(i).getArray().get(j);
                    if (game.isSelected()) {
                        return Grids.get(i);
                    }
                } catch (RuntimeException ex) {
                }
            }

        }
        return null;
    }

    /**
     * Returns an Array filled with all AGridPanel's
     *
     * @return Grid ArrayList
     *
     */
    public ArrayList getArray() {
        return Grids;
    }

    public int getFullGrids() {
        return fullGrids;
    }

    public int getNumberOfGrids() {
        return Grids.size();
    }
}
