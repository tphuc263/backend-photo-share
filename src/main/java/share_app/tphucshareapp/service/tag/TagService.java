package share_app.tphucshareapp.service.tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.model.PhotoTag;
import share_app.tphucshareapp.model.Tag;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.repository.PhotoTagRepository;
import share_app.tphucshareapp.repository.TagRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService implements ITagService {
    private final TagRepository tagRepository;
    private final PhotoTagRepository photoTagRepository;

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
    public void addTagsToPhoto(String photoId, List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }

        List<PhotoTag> photoTags = tags.stream()
                .map(tag -> createPhotoTag(photoId, tag.getId()))
                .toList();

        try {
            photoTagRepository.saveAll(photoTags);
            log.info("Added {} tags to photo {}", photoTags.size(), photoId);
        } catch (Exception e) {
            log.warn("Some tags might already exist for photo {}", photoId);
            // Handle duplicate key errors individually
            for (PhotoTag photoTag : photoTags) {
                try {
                    photoTagRepository.save(photoTag);
                } catch (Exception ignored) {
                    // Tag already exists for this photo, skip
                }
            }
        }
    }

    @Override
    public void removeTagsFromPhoto(String photoId) {
        List<PhotoTag> photoTags = photoTagRepository.findByPhotoId(photoId);
        if (!photoTags.isEmpty()) {
            photoTagRepository.deleteAll(photoTags);
            log.info("Removed {} tags from photo {}", photoTags.size(), photoId);
        }
    }

    @Override
    public List<Tag> getPhotoTags(String photoId) {
        List<PhotoTag> photoTags = photoTagRepository.findByPhotoId(photoId);
        List<String> tagIds = photoTags.stream()
                .map(PhotoTag::getTagId)
                .toList();

        return tagRepository.findAllById(tagIds);
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

    // Helper methods
    private Tag createTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setCreatedAt(Instant.now());
        return tag;
    }

    private PhotoTag createPhotoTag(String photoId, String tagId) {
        PhotoTag photoTag = new PhotoTag();
        photoTag.setPhotoId(photoId);
        photoTag.setTagId(tagId);
        photoTag.setCreatedAt(Instant.now());
        return photoTag;
    }
}
