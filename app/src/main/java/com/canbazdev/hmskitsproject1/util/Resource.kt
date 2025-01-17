package com.canbazdev.hmskitsproject1.util

/*
*   Created by hamzacanbaz on 7/21/2022
*/
sealed class Resource<T>(val data: T? = null, val errorMessage: String? = null) {
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(errorMessage: String, data: T? = null) : Resource<T>(data, errorMessage)
}