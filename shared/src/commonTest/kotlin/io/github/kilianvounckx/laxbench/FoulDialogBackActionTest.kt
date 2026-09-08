package io.github.kilianvounckx.laxbench

import kotlin.test.Test
import kotlin.test.assertEquals

class FoulDialogBackActionTest {

  @Test
  fun `BATCH_CONFIRMATION always commits the pending batch and dismisses, regardless of batch emptiness`() {
    assertEquals(
      FoulDialogBackAction.COMMIT_PENDING_BATCH_AND_DISMISS,
      foulDialogBackAction(FoulDialogStepKind.BATCH_CONFIRMATION, pendingBatchIsEmpty = true),
    )
    assertEquals(
      FoulDialogBackAction.COMMIT_PENDING_BATCH_AND_DISMISS,
      foulDialogBackAction(FoulDialogStepKind.BATCH_CONFIRMATION, pendingBatchIsEmpty = false),
    )
  }

  @Test
  fun `WITH_PREVIOUS always goes to the previous step, regardless of batch emptiness`() {
    assertEquals(
      FoulDialogBackAction.GO_TO_PREVIOUS_STEP,
      foulDialogBackAction(FoulDialogStepKind.WITH_PREVIOUS, pendingBatchIsEmpty = true),
    )
    assertEquals(
      FoulDialogBackAction.GO_TO_PREVIOUS_STEP,
      foulDialogBackAction(FoulDialogStepKind.WITH_PREVIOUS, pendingBatchIsEmpty = false),
    )
  }

  @Test
  fun `FIRST dismisses when the pending batch is empty`() {
    assertEquals(
      FoulDialogBackAction.DISMISS,
      foulDialogBackAction(FoulDialogStepKind.FIRST, pendingBatchIsEmpty = true),
    )
  }

  @Test
  fun `FIRST goes to the confirm-cancel choice when the pending batch is not empty`() {
    assertEquals(
      FoulDialogBackAction.GO_TO_CONFIRM_CANCEL_CHOICE,
      foulDialogBackAction(FoulDialogStepKind.FIRST, pendingBatchIsEmpty = false),
    )
  }
}
