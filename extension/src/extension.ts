import * as vscode from 'vscode';
import { BridgeReceiver } from './bridge/receiver';
import { applyPatch } from './diff/patchApplier';

export function activate(context: vscode.ExtensionContext) {
    console.log('RelayPatch extension activated.');

    const receiver = new BridgeReceiver(vscode);
    const disposable = receiver.onPatchReceived(async (patch) => {
        const editor = vscode.window.activeTextEditor;
        if (!editor) {
            vscode.window.showErrorMessage('RelayPatch: No active editor to apply diff.');
            return;
        }
        const success = await applyPatch(patch, editor.document);
        if (success) {
            vscode.window.showInformationMessage(`RelayPatch: Applied diff successfully.`);
        } else {
            vscode.window.showErrorMessage(`RelayPatch: Failed to apply diff.`);
        }
    });

    receiver.start();

    context.subscriptions.push(disposable);
    context.subscriptions.push({ dispose: () => receiver.stop() });

    const command = vscode.commands.registerCommand('relaypatch.openDiff', () => {
        vscode.window.showInformationMessage('RelayPatch diff viewer opened.');
    });

    context.subscriptions.push(command);
}

export function deactivate() {}
