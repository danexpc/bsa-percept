# BSA Percept
 The web service to search for similar images.

  The service supports 4 operations:
  1. Upload several images to the base for recognition
  2. Upload 1 image to search. If it was not found, then add it to the database
  3. Delete an image from the database by id
  4. Delete all images from the database
  
 Downloaded images are saved to your hard drive. The calculated hash with id and path to the image is saved to the database.
 File system operations have been moved to a service that provides an asynchronous API.
 
 When processing a batch request, writing to the hard disk and calculating the hash occur in parallel, after both operations are completed, a record is created with information about the image in the database. After saving all the images in the array, the response is returned to the client
 
 
 In the case of an image search, if no match was found, as well as during batch loading, a file is written to disk and a record is created in the database, but the response is sent without waiting for the end of these operations

 The app provides 4 endpoints:
 
 ```
 POST /image/batch
 content-type: multipart/form-data
 images: MultipartFile[]
 ```
 Downloads multiple files, reads the hash, saves the images to disk, and creates a database entry.
 
 ```
 POST /image/search?threshold=?
 content-type: multipart/form-data
 image: MultipartFile
 
 Response: [
    {
        id: "92c73b0f-77d6-41b9-be87-1e0ebf20be31",
        image: "http://127.0.0.1:8080/files/92c73b0f-77d6-41b9-be87-1e0ebf20be31.jpg",
        match: 96.1265
    }
 ]
 ```
 
 Loads a file that is searched for in the database with the specified minimum match percentage (threshold). Threshold should be in the range (0, 1], if it is absent, use 0.9 as the default value. If no similar images are found, then save it to disk and add a record to persistent storage.

 ```
 DELETE /image/{id}
 ```
 
 Removes the specified image from the database and hard drive.
 
 ```
 DELETE /image/purge
 ```
 
 Removes all images from the hard drive and from the database
 
 
 The application also provides access to the saved images by link.
