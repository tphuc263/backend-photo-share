package share_app.tphucshareapp.service.tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.model.Tag;
import share_app.tphucshareapp.repository.TagRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService implements ITagService {
    private final TagRepository tagRepository;

    @Override
    public List<Tag> createOrGetTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }

        // Normalize tag names (lowercase, trim)
        Set<String> normalizedNames = tagNames.stream()
                .map(name -> name.trim().toLowerCase())
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toSet());

        if (normalizedNames.isEmpty()) {
            return List.of();
        }

        // Find existing tags
        List<Tag> existingTags = tagRepository.findByNameIn(normalizedNames);
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        // Create new tags for names that don't exist
        List<Tag> newTags = normalizedNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .map(this::createTag)
                .toList();

        if (!newTags.isEmpty()) {
            tagRepository.saveAll(newTags);
            log.info("Created {} new tags", newTags.size());
        }

        // Combine existing and new tags
        List<Tag> allTags = new java.util.ArrayList<>(existingTags);
        allTags.addAll(newTags);

        return allTags;
    }


    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Override
    public List<Tag> searchTags(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String normalizedQuery = query.trim().toLowerCase();
        return tagRepository.findByNameContainingIgnoreCase(normalizedQuery);
    }

    // helper methods
    private Tag createTag(String newTagName) {
        Tag newTag = new Tag();
        newTag.setName(newTagName);
        return tagRepository.save(newTag);
    }
}
