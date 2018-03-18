package com.comp4111.controllers;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.comp4111.models.Book;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

@Path("books")
public class BookManagement {
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference booksRef = database.getReference("books");

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Book getBook () {
        Book book = new Book();
        book.Title("XYZ");
        return book;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void createBook (final Book book, @Suspended final AsyncResponse response) {
        // Map<String, Object> books = new HashMap<>();
        // books.put(book.id, book.serialize());

        booksRef.orderByChild("title").equalTo(book.title).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.hasChildren()) {
                    try {
                        DatabaseReference bookRef = booksRef.child(book.getId());
                        bookRef.setValue(book.serialize(), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                System.out.println("Data could not be saved " + databaseError.getMessage());
                                } else {
                                    System.out.println("Data saved successfully.");
                                    try {
                                        response.resume(
                                            Response
                                            .status(Response.Status.CREATED)
                                            .entity(book)
                                            .location(new URI("books/" + book.getId()))
                                            .build()
                                        );
                                    } catch (Exception e) {
                                        System.out.println("cant send response with error: " + e);
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        System.out.println("cant set with error: " + e);
                    }
                    
                } else {
                    try {
                        response.resume(
                            Response
                            .status(Response.Status.CONFLICT)
                            .header("duplicate-record", "books/" + snapshot.getChildren().iterator().next().getValue(Book.class).getId())
                            .location(new URI("books/" + snapshot.getChildren().iterator().next().getValue(Book.class).getId()))
                            .build()
                        );
                    } catch (Exception e) {
                        System.out.println("cant send response with error: " + e);
                    }
                }
            }
        
            @Override
            public void onCancelled(DatabaseError error) {
                
            }
        });
    }
}
