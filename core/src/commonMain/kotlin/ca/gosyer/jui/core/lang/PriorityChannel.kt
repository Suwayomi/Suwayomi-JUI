/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.core.lang

import com.soywiz.kds.PriorityQueue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.SelectClause1
import kotlinx.coroutines.selects.SelectClause2
import kotlinx.coroutines.yield

// Based on https://github.com/kerubistan/kroki/blob/master/kroki-coroutines/src/main/kotlin/io/github/kerubistan/kroki/coroutines/Channels.kt

/**
 * Hides a coroutine between two channels, uniting them as a single channel.
 */
internal open class ProcessChannel<T>(
    internal val inChannel: Channel<T>,
    internal val outChannel: Channel<T>,
) : Channel<T> {

    @DelicateCoroutinesApi
    override val isClosedForReceive: Boolean
        get() = outChannel.isClosedForReceive

    @DelicateCoroutinesApi
    override val isClosedForSend: Boolean
        get() = inChannel.isClosedForSend

    @ExperimentalCoroutinesApi
    override val isEmpty: Boolean
        get() = outChannel.isEmpty

    override val onReceive: SelectClause1<T> get() = outChannel.onReceive

    override val onSend: SelectClause2<T, SendChannel<T>> get() = inChannel.onSend

    @Deprecated(
        "Since 1.2.0, binary compatibility with versions <= 1.1.x",
        level = DeprecationLevel.HIDDEN,
    )
    override fun cancel(cause: Throwable?): Boolean {
        outChannel.cancel()
        return true
    }

    override fun cancel(cause: CancellationException?) = outChannel.cancel(cause)

    override fun close(cause: Throwable?): Boolean = inChannel.close(cause)

    @ExperimentalCoroutinesApi
    override fun invokeOnClose(handler: (cause: Throwable?) -> Unit) {
        inChannel.invokeOnClose(handler)
    }

    override fun iterator(): ChannelIterator<T> = outChannel.iterator()

    @Deprecated(
        "Deprecated in the favour of 'trySend' method",
        replaceWith = ReplaceWith("trySend(element).isSuccess"),
        level = DeprecationLevel.ERROR,
    )
    override fun offer(element: T): Boolean = inChannel.trySend(element).isSuccess

    @Deprecated(
        "Deprecated in the favour of 'tryReceive'. Please note that the provided replacement does not rethrow channel's close cause as 'poll' did, for the precise replacement please refer to the 'poll' documentation",
        replaceWith = ReplaceWith("tryReceive().getOrNull()"),
        level = DeprecationLevel.ERROR,
    )
    override fun poll(): T? = outChannel.tryReceive().getOrNull()

    override suspend fun receive(): T = outChannel.receive()

    override suspend fun send(element: T) = inChannel.send(element)
    override val onReceiveCatching: SelectClause1<ChannelResult<T>>
        get() = TODO("not implemented")

    override suspend fun receiveCatching(): ChannelResult<T> {
        TODO("not implemented")
    }

    override fun tryReceive(): ChannelResult<T> {
        TODO("not implemented")
    }

    override fun trySend(element: T): ChannelResult<Unit> {
        TODO("not implemented")
    }
}

@OptIn(DelicateCoroutinesApi::class)
@ExperimentalCoroutinesApi
internal class PriorityChannelImpl<T>(
    private val maxCapacity: Int,
    scope: CoroutineScope,
    comparator: Comparator<T>,
) : ProcessChannel<T>(
    // why a rendezvous channel should be the input channel?
    // because we buffer and sort the messages in the co-routine
    // that is where the capacity constraint is enforced
    // and the buffer we keep sorted, the input channel we can't
    inChannel = Channel(Channel.RENDEZVOUS),
    // output channel is rendezvous channel because we may still
    // get higher priority input meanwhile and we will send that
    // when output consumer is ready to take it
    outChannel = Channel(Channel.RENDEZVOUS),
) {
    private val buffer = PriorityQueue(comparator)

    private fun PriorityQueue<T>.isNotFull() = this.size < maxCapacity

    private fun PriorityQueue<T>.isFull() = this.size >= maxCapacity

    // non-suspending way to get all messages available at the moment
    // as long as we have anything to receive and the buffer is not full
    // we should keep receiving
    private fun tryGetSome() {
        if (buffer.isNotFull()) {
            var received = inChannel.tryReceive().getOrNull()
            if (received != null) {
                buffer.add(received)
                while (buffer.isNotFull() && received != null) {
                    received = inChannel.tryReceive().getOrNull()
                    received?.let { buffer.add(it) }
                }
            }
        }
    }

    private suspend fun getAtLeastOne() {
        buffer.add(inChannel.receive())
        tryGetSome()
    }

    private suspend fun trySendSome() {
        when {
            buffer.isEmpty() -> {
                yield()
            }
            buffer.isFull() -> {
                outChannel.send(buffer.removeHead())
            }
            else -> {
                while (buffer.isNotEmpty() && outChannel.trySend(buffer.head).isSuccess) {
                    buffer.removeHead()
                    tryGetSome()
                }
            }
        }
    }

    private suspend fun sendAll() {
        while (buffer.isNotEmpty()) {
            outChannel.send(buffer.removeHead())
        }
    }

    init {
        require(maxCapacity >= 2) {
            "priorityChannel maxCapacity < 2 does not make any sense"
        }

        scope.async {
            try {
                getAtLeastOne()

                while (!inChannel.isClosedForReceive) {
                    trySendSome()
                    tryGetSome()
                }
            } finally {
                // input channel closed, send the buffer to out channel
                sendAll()
                // and finally close the output channel, signaling that that this was it
                outChannel.close()
            }
        }.start()
    }
}

/**
 * Creates a channel that always outputs the highest priority element received so far.
 * It is important to note here that while the coroutine API channels are all FIFO, this
 * one is not.
 * @param maxCapacity the number of items the channel can keep inside
 * @param scope coroutine-scope to run the sorting in
 * @param comparator a comparator for the
 */
@ExperimentalCoroutinesApi
fun <T> PriorityChannel(
    maxCapacity: Int = 4096,
    scope: CoroutineScope,
    comparator: Comparator<T>,
): Channel<T> = PriorityChannelImpl(maxCapacity, scope, comparator)
