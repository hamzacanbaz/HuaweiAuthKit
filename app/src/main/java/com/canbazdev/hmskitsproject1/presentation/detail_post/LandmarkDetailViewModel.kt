package com.canbazdev.hmskitsproject1.presentation.detail_post

import android.app.Application
import android.content.Context
import android.util.Log
import android.util.SparseArray
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.canbazdev.hmskitsproject1.domain.model.landmark.Post
import com.canbazdev.hmskitsproject1.domain.usecase.location.GetNearbySitesUseCase
import com.canbazdev.hmskitsproject1.domain.usecase.login.InsertUserToFirebaseUseCase
import com.canbazdev.hmskitsproject1.domain.usecase.posts.GetLandmarkWithIdUseCase
import com.canbazdev.hmskitsproject1.domain.usecase.posts.InsertLandmarkToWishListUseCase
import com.canbazdev.hmskitsproject1.util.ActionState
import com.canbazdev.hmskitsproject1.util.Constants
import com.canbazdev.hmskitsproject1.util.Resource
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.hms.kit.awareness.Awareness
import com.huawei.hms.kit.awareness.barrier.TimeBarrier
import com.huawei.hms.kit.awareness.capture.TimeCategoriesResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


/*
*   Created by hamzacanbaz on 7/27/2022
*/
@HiltViewModel
class LandmarkDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getNearbySitesUseCase: GetNearbySitesUseCase,
    private val insertUserToFirebaseUseCase: InsertUserToFirebaseUseCase,
    private val getLandmarkWithIdUseCase: GetLandmarkWithIdUseCase,
    private val insertLandmarkToWishListUseCase: InsertLandmarkToWishListUseCase,
    application: Application
) : AndroidViewModel(application) {

    private val _landmark = MutableStateFlow(Post())
    val landmark: StateFlow<Post> = _landmark

    private val _actionState = MutableStateFlow<ActionState?>(null)
    val actionState: StateFlow<ActionState?> = _actionState


    init {
        getDetailLandmarkFromBundle()
    }

    private fun getDetailLandmarkFromBundle() {
        savedStateHandle.get<Post>("landmark")?.let { post ->
            _landmark.value = post
            getNearbyPlaces()
        }
        savedStateHandle.get<String>(Constants.SCAN_UUID)?.let { id ->
            viewModelScope.launch {
                getLandmarkWithIdUseCase.invoke(id).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null && result.data != Post()) {
                                _landmark.value = result.data
                            } else {
                                _actionState.value = ActionState.NavigateToHome
                            }

                        }
                        is Resource.Loading -> {}
                        is Resource.Error -> println("DETAIL ERROR")
                    }
                }
            }
        }
    }

    private fun getNearbyPlaces() {
        viewModelScope.launch {
            landmark.value.landmarkLatitude
                ?.let {
                    landmark.value.landmarkLongitude?.let { it1 ->
                        getNearbySitesUseCase.invoke(it, it1)
                    }
                }?.collect { resource ->
                    when (resource) {
                        is Resource.Success -> println("successssss" + resource.data?.size)
                        is Resource.Error -> println("erollllllll" + resource.errorMessage)
                        is Resource.Loading -> println("loaddddd")
                    }
                }
        }
    }

    // TODO UID KISMINI DEGISTIR
     fun insertLandmarkToWishList() {
        viewModelScope.launch {
            insertLandmarkToWishListUseCase.invoke(
                AGConnectAuth.getInstance().currentUser.uid,
                landmark.value
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> println("wish list loading")
                    is Resource.Success -> println("wish list success")
                    is Resource.Error -> println("wish list error : ${result.errorMessage.toString()}")
                }
            }
        }
    }

}