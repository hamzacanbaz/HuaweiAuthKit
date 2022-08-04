package com.canbazdev.hmskitsproject1.domain.usecase.posts

import com.canbazdev.hmskitsproject1.domain.model.landmark.Post
import com.canbazdev.hmskitsproject1.domain.source.RemoteDataSource
import com.canbazdev.hmskitsproject1.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/*
*   Created by hamzacanbaz on 8/4/2022
*/
class InsertLandmarkToWishListUseCase @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    operator fun invoke(id: String, post: Post): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            emit(Resource.Success(remoteDataSource.insertLandmarkToWishList(id, post)))
        } catch (e: Exception) {
            emit(Resource.Error(errorMessage = e.localizedMessage ?: e.message!!))
        }


    }

}