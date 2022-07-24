package com.noice.dextro.utils

class NetworkCallStatus<T>(var status: Status? = null, var data: T? = null, var msg:String? = null) {
    companion object {
        fun <T>success(data: T?, msg: String? = null): NetworkCallStatus<T> {
            return NetworkCallStatus(
                Status.SUCCESS,
                data,
                msg
            )
        }

        fun <T>loading(data: T? = null, msg: String? = null): NetworkCallStatus<T> {
            return NetworkCallStatus(
                Status.LOADING,
                data,
                msg
            )
        }

        fun <T>error(data: T? = null, msg: String? = null): NetworkCallStatus<T> {
            return NetworkCallStatus(
                Status.ERROR,
                data,
                msg
            )
        }
    }
    enum class Status {
        LOADING,
        SUCCESS,
        ERROR
    }
}