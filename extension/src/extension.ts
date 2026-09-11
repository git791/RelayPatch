import * as vscode from 'vscode';
import { BridgeReceiver } from './bridge/receiver';
import { openDiffView } from './diff/diffView';

export function activate(context: vscode.ExtensionContext) {
    console.log('RelayPatch extension activated.');

    const receiver = new BridgeReceiver(vscode);
    const disposable = receiver.onPatchReceived(async (patch) => {
        await openDiffView(patch, context);
    });

    receiver.start();

    context.subscriptions.push(disposable);
    context.subscriptions.push({ dispose: () => receiver.stop() });

    const commandOpenDiff = vscode.commands.registerCommand('relaypatch.openDiff', () => {
        vscode.window.showInformationMessage('RelayPatch diff viewer opened.');
    });

    const commandStart = vscode.commands.registerCommand('relaypatch.startBridgeListener', () => {
        receiver.start();
        vscode.window.showInformationMessage('RelayPatch: Bridge Listener started.');
    });

    const commandStop = vscode.commands.registerCommand('relaypatch.stopBridgeListener', () => {
        receiver.stop();
        vscode.window.showInformationMessage('RelayPatch: Bridge Listener stopped.');
    });

    context.subscriptions.push(commandOpenDiff, commandStart, commandStop);
}

export function deactivate() {}
