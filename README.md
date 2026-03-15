# crudProject
1. Short Description

This application serves as a personal cookbook, allowing users to easily manage all their recipes. The main screen lists all saved recipes, enabling users to Create new ones, Read the detailed instructions, Update ingredients or steps, and Delete recipes they no longer need. A core benefit is the application's ability to operate fully offline. Whether editing a recipe or adding a new one in the kitchen, all changes are saved locally. Once internet connectivity is restored, the application handles the automatic synchronization of all queued operations with the remote server, ensuring your recipe library is always backed up and consistent.It’s ideal for anyone who enjoys cooking and wants to keep their recipes organized, accessible, and safe.

2. Domain Details
Each recipe represents one cooking entry created by the user.  
recipeId     	Integer     (Primary Key)	A unique identifier for the recipe, assigned by the system (server).<br/>
title	        String	    The common name of the dish.<br/>
ingredients  	String	    A detailed, structured list of all required ingredients and their quantities.<br/>
instructions	String	    The complete step-by-step cooking directions.<br/>
prepTimeMinutes	Integer	    The required time for preparation in minutes (e.g., 30).<br/>
servings	    Integer	    The intended number of people the recipe will feed (e.g., 4).

3. CRUD. Present the details of each crud operation.

Create (Add)	    Add a new recipe with title, ingredients, instructions, prep time, and servings.
Read (Display)	    Display all recipes in a list. Tap one to view details.
Update (Edit)	    Edit any recipe’s fields, such as adjusting prep time or servings.
Delete (Remove)	    Remove a recipe from the collection.

4. Persistence details, what crud operations are persisted on the local db and on the server.

### Local Database
- Keeps all recipes accessible offline.  
- Stores any changes made while offline (create, update, delete).  
- Ensures changes survive app restarts.  

### Server Database
- Remote server stores a central copy of all recipes.  
- REST endpoints:  
  - `POST /recipes` → Create  
  - `GET /recipes` → Read  
  - `PUT /recipes/{recipeId}` → Update  
  - `DELETE /recipes/{recipeId}` → Delete 


5. Offline Scenario
Create (Add)	    New recipe is saved locally as “Pending Sync.”
Read (Display)	    List shows locally stored recipes with “Offline Mode” banner.
Update (Edit)	    Edits are saved locally and queued for synchronization.
Delete (Remove)	    Recipe is removed locally and flagged for deletion.

Online: All recipes management operations are available and changes are immediately saved to the server.
Offline: Users can continue interacting with recipes, with changes synced once online access is restored. Since this app is meant to increase productivity in a creative field, all operations are going to still be available, with the synchronization with the server is mostly used as a back-up service and as a way to access the data on desktop browsers.
