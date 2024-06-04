package com.HomeWork6.HomeWork6.conroller;

import com.HomeWork6.HomeWork6.model.Note;
import com.HomeWork6.HomeWork6.service.FileGateway;
import com.HomeWork6.HomeWork6.repository.NoteRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notes")
public class NoteController {

    private final Counter addNoteCounter = Metrics.counter("add_note_count");
    private final FileGateway fileGateway;
    private final NoteRepository noteRepository;

    @Autowired
    public NoteController(FileGateway fileGateway, NoteRepository noteRepository) {
        this.fileGateway = fileGateway;
        this.noteRepository = noteRepository;
    }

    @PostMapping
    public ResponseEntity<?> addNote(@RequestBody Note note) {
        if (note.getTitle() == null || note.getTitle().trim().isEmpty()) {
            return new ResponseEntity<>("Ошибка: Пустое поле заголовка", HttpStatus.BAD_REQUEST);
        }
        if (note.getContent() == null || note.getContent().trim().isEmpty()) {
            return new ResponseEntity<>("Ошибка: Пустое поле содержимого", HttpStatus.BAD_REQUEST);
        }
        Note savedNote = noteRepository.save(note);
        addNoteCounter.increment();
        fileGateway.writeToFile(note.getTitle() + ".txt", note.toString());
        return new ResponseEntity<>(savedNote, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes() {
        List<Note> notes = noteRepository.findAll();
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        Optional<Note> optionalNote = noteRepository.findById(id);
        return optionalNote.map(note -> new ResponseEntity<>(note, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNoteById(@PathVariable Long id, @RequestBody Note updatedNote) {
        if (updatedNote.getTitle() == null || updatedNote.getTitle().trim().isEmpty()) {
            return new ResponseEntity<>("Ошибка: Пустое поле заголовка", HttpStatus.BAD_REQUEST);
        }
        if (updatedNote.getContent() == null || updatedNote.getContent().trim().isEmpty()) {
            return new ResponseEntity<>("Ошибка: Пустое поле содержимого", HttpStatus.BAD_REQUEST);
        }

        Optional<Note> optionalNote = noteRepository.findById(id);
        if (optionalNote.isPresent()) {
            updatedNote.setId(id);
            Note savedNote = noteRepository.save(updatedNote);
            return new ResponseEntity<>(savedNote, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNoteById(@PathVariable Long id) {
        Optional<Note> optionalNote = noteRepository.findById(id);
        if (optionalNote.isPresent()) {
            noteRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}