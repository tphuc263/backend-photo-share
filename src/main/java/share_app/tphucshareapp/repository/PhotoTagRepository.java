package share_app.tphucshareapp.repository;

public interface PhotoTagRepository extends MongoRepository<PhotoTag, String> {

    void deleteByPhotoId(String photoId);

    @Query(value = "{ 'tagId': ?0 }", fields = "{ 'photoId': 1 }")
    List<String> findPhotoIdsByTagId(String tagId);

    @Query(value = "{ 'photoId': ?0 }", fields = "{ 'tagId': 1 }")
    List<String> findTagIdsByPhotoId(String photoId);

    @Query(pipeline = {
            "{ $match: { 'photoId': ?0 } }",
            "{ $lookup: { from: 'tags', localField: 'tagId', foreignField: '_id', as: 'tag' } }",
            "{ $unwind: '$tag' }",
            "{ $project: { '_id': 0, 'name': '$tag.name' } }"
    })
    List<String> findTagNamesByPhotoId(String photoId);
}