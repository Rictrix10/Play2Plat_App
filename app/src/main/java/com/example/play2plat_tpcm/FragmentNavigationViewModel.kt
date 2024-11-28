package com.ddkric.play2plat

import androidx.lifecycle.ViewModel
import androidx.fragment.app.Fragment

class FragmentNavigationViewModel : ViewModel() {
    private val fragmentStack = mutableListOf<Fragment>()

    fun getFragmentStack(): List<Fragment> = fragmentStack.toList()

    fun addToStack(fragment: Fragment) {
        fragmentStack.add(fragment)
    }

    fun removeFromStack(fragment: Fragment) {
        fragmentStack.remove(fragment)
    }

    fun clearStack() {
        fragmentStack.clear()
    }
}
